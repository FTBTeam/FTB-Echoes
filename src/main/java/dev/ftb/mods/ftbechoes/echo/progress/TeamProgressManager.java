package dev.ftb.mods.ftbechoes.echo.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftbechoes.net.ClaimRewardResponseMessage;
import dev.ftb.mods.ftbechoes.net.SyncProgressMessage;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import dev.ftb.mods.ftbechoes.shopping.ShoppingKey;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.function.Function;

public class TeamProgressManager extends SavedData {
    private static final String SAVE_NAME = FTBEchoes.MOD_ID + "_progress";

    // serialization!  using xmap here, so we get mutable hashmaps in the live manager
    private static final Codec<Map<UUID, TeamProgress>> PROGRESS_CODEC
            = Codec.unboundedMap(UUIDUtil.STRING_CODEC, TeamProgress.CODEC).xmap(HashMap::new, Map::copyOf);

    public static final Codec<TeamProgressManager> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            PROGRESS_CODEC.fieldOf("progress").forGetter(mgr -> mgr.progressMap)
    ).apply(builder, TeamProgressManager::new));

    // keyed by Team ID (not player ID)
    private final Map<UUID, TeamProgress> progressMap;

    private TeamProgressManager(Map<UUID,TeamProgress> progressMap) {
        this.progressMap = progressMap;
    }

    public static TeamProgressManager get() {
        return get(Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer()));
    }

    public static TeamProgressManager get(MinecraftServer server) {
        DimensionDataStorage dataStorage = Objects.requireNonNull(server.getLevel(Level.OVERWORLD)).getDataStorage();

        return dataStorage.computeIfAbsent(factory(), SAVE_NAME);
    }

    private static SavedData.Factory<TeamProgressManager> factory() {
        return new SavedData.Factory<>(TeamProgressManager::createNew, TeamProgressManager::load, null);
    }

    private static TeamProgressManager createNew() {
        return new TeamProgressManager(new HashMap<>());
    }

    private static TeamProgressManager load(CompoundTag tag, HolderLookup.Provider provider) {
        return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag.getCompound("progress"))
                .resultOrPartial(err -> FTBEchoes.LOGGER.error("failed to deserialize progress data: {}", err))
                .orElse(TeamProgressManager.createNew());
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return Util.make(new CompoundTag(), tag -> {
            var t = CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this)
                    .resultOrPartial(err -> FTBEchoes.LOGGER.error("failed to serialize progress data: {}", err))
                    .orElse(new CompoundTag());

            tag.put("progress", t);
        });
    }

    public Optional<TeamProgress> getProgress(ServerPlayer sp) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(sp).map(this::getProgress);
    }

    public TeamProgress getProgress(Team team) {
        return get().progressMap.computeIfAbsent(team.getTeamId(), k -> newProgress());
    }

    public boolean claimReward(ServerPlayer player, ResourceLocation echoId, int stageIdx) {
        return applyChange(player, progress -> progress.claimReward(echoId, player, stageIdx));
    }

    public boolean setStage(ServerPlayer player, ResourceLocation echoId, int stageIdx) {
        return applyChange(player, progress -> progress.setStage(echoId, stageIdx));
    }

    public boolean setStage(Team team, ResourceLocation echoId, int stageIdx) {
        return applyChange(team, progress -> progress.setStage(echoId, stageIdx));
    }

    public boolean resetReward(UUID playerId, ResourceLocation echoId, int stageIdx) {
        return applyChange(playerId, progress -> progress.resetReward(echoId, playerId, stageIdx));
    }

    public void tryCompleteStage(ServerPlayer sp, Team team, Echo echo) {
        TeamProgress teamProgress = TeamProgressManager.get().getProgress(team);
        final int currentStage = teamProgress.getCurrentStage(echo.id());

        if (currentStage >= 0 && currentStage < echo.stages().size()) {
            EchoStage stage = echo.stages().get(currentStage);
            // TODO update to:
            //if (TeamStages.hasTeamStage(team, stage.requiredGameStage()) && completeStage(team, echo)) {
            if (FTBEchoes.stageProvider().has(sp, stage.requiredGameStage()) && completeStage(team, echo)) {
                notifyTeamCompletion(team, echo, currentStage);
            }
            stage.completionReward().ifPresent(reward -> {
                if (reward.autoclaim()) {
                    if (teamProgress.claimReward(echo.id(), sp, currentStage)) {
                        PacketDistributor.sendToPlayer(sp, SyncProgressMessage.forPlayer(teamProgress, sp));
                        PacketDistributor.sendToPlayer(sp, new ClaimRewardResponseMessage(true, Optional.ofNullable(stage.completionRewardSummary())));
                        setDirty();
                    }
                }
            });
        }
    }

    private void notifyTeamCompletion(Team team, Echo echo, int currentStage) {
        team.getOnlineMembers().forEach(member -> {
            Vec3 vec = member.position();
            Component echoTitle = echo.title().copy().withStyle(ChatFormatting.YELLOW);
            Component stageTitle = echo.stages().get(currentStage).title().copy().withStyle(ChatFormatting.YELLOW);
            member.displayClientMessage(Component.translatable("ftbechoes.message.echo_stage_complete", echoTitle, stageTitle).withStyle(ChatFormatting.GREEN), false);
            if (currentStage == echo.stages().size() - 1) {
                member.displayClientMessage(Component.translatable("ftbechoes.message.echo_complete", echoTitle).withStyle(ChatFormatting.LIGHT_PURPLE), false);
                member.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE),
                        SoundSource.PLAYERS, vec.x, vec.y, vec.z, 1f, 1f, 0L));
            } else {
                member.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP),
                        SoundSource.PLAYERS, vec.x, vec.y, vec.z, 1f, 1f, 0L));
            }
        });
    }

    private boolean completeStage(Team team, Echo echo) {
        return applyChange(team, progress -> progress.completeStage(echo));
    }

    public boolean resetAllRewards(UUID playerId, ResourceLocation echoId) {
        return applyChange(playerId, progress -> progress.resetAllRewards(echoId, playerId));
    }

    public void consumeLimitedShopPurchase(ServerPlayer player, ShoppingKey key, int count, ShopData shopData) {
        applyChange(player, progress -> {
            progress.consumeShopStock(player, key, count, shopData);
            return true;
        });
    }

    public boolean resetShopStock(ServerPlayer player, ResourceLocation echoId) {
        return applyChange(player, progress -> progress.resetShopStock(echoId));
    }

    public boolean resetShopStock(Team team, ResourceLocation echoId) {
        return applyChange(team, progress -> progress.resetShopStock(echoId));
    }

    private boolean applyChange(UUID playerId, Function<TeamProgress, Boolean> task) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerId)
                .map(team -> applyChange(team, task))
                .orElse(false);
    }

    private boolean applyChange(ServerPlayer player, Function<TeamProgress, Boolean> task) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                .map(team -> applyChange(team, task))
                .orElse(false);
    }

    private boolean applyChange(Team team, Function<TeamProgress, Boolean> task) {
        TeamProgress teamProgress = progressMap.computeIfAbsent(team.getId(), k -> newProgress());
        if (task.apply(teamProgress)) {
            setDirty();
            team.getOnlineMembers().forEach(player ->
                    PacketDistributor.sendToPlayer(player, SyncProgressMessage.forPlayer(teamProgress, player))
            );
            return true;
        }
        return false;
    }

    private TeamProgress newProgress() {
        setDirty();
        return TeamProgress.createNew();
    }

    public void injectProgressData(UUID teamId, TeamProgress progress) {
        // called via the /ftbechoes nbtedit command
        progressMap.put(teamId, progress);
        setDirty();
        FTBTeamsAPI.api().getManager().getTeamByID(teamId)
                .ifPresent(team -> team.getOnlineMembers().forEach(sp ->
                        PacketDistributor.sendToPlayer(sp, SyncProgressMessage.forPlayer(progress, sp)))
                );
    }
}
