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
import dev.ftb.mods.ftbechoes.util.MiscUtil;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class TeamProgressManager extends SavedData {
//    private static final String SAVE_NAME = FTBEchoes.MOD_ID + "_progress";

    // serialization!  using xmap here, so we get mutable hashmaps in the live manager
    private static final Codec<Map<UUID, TeamProgress>> PROGRESS_CODEC
            = Codec.unboundedMap(UUIDUtil.STRING_CODEC, TeamProgress.CODEC).xmap(HashMap::new, Map::copyOf);

    public static final Codec<TeamProgressManager> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            PROGRESS_CODEC.fieldOf("progress").forGetter(mgr -> mgr.progressMap)
    ).apply(builder, TeamProgressManager::new));

    public static final SavedDataType<TeamProgressManager> TYPE
            = new SavedDataType<>(FTBEchoes.id("progress"), TeamProgressManager::createNew, CODEC);

    // keyed by Team ID (not player ID)
    private final Map<UUID, TeamProgress> progressMap;

    private TeamProgressManager(Map<UUID,TeamProgress> progressMap) {
        this.progressMap = progressMap;
    }

    public static TeamProgressManager get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    private static TeamProgressManager createNew() {
        return new TeamProgressManager(new HashMap<>());
    }

    public Optional<TeamProgress> getProgress(ServerPlayer sp) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(sp).map(this::getProgress);
    }

    public TeamProgress getProgress(Team team) {
        return progressMap.computeIfAbsent(team.getTeamId(), k -> newProgress());
    }

    public boolean claimReward(ServerPlayer player, Identifier echoId, int stageIdx) {
        return applyChange(player, progress -> progress.claimReward(echoId, player, stageIdx));
    }

    public boolean setStage(ServerPlayer player, Identifier echoId, int stageIdx) {
        return applyChange(player, progress -> progress.setStage(echoId, stageIdx));
    }

    public boolean setStage(Team team, Identifier echoId, int stageIdx) {
        return applyChange(team, progress -> progress.setStage(echoId, stageIdx));
    }

    public boolean resetReward(UUID playerId, Identifier echoId, int stageIdx) {
        return applyChange(playerId, progress -> progress.resetReward(echoId, playerId, stageIdx));
    }

    public void tryCompleteStage(ServerPlayer sp, Team team, Echo echo) {
        TeamProgress teamProgress = TeamProgressManager.get(sp.level().getServer()).getProgress(team);
        final int currentStage = teamProgress.getCurrentStage(echo.id());

        if (currentStage >= 0 && currentStage < echo.stages().size()) {
            EchoStage stage = echo.stages().get(currentStage);
            if (MiscUtil.hasStage(sp, team, stage.requiredGameStage())) {
                stage.completionReward().ifPresent(reward -> {
                    if (reward.autoclaim()) {
                        if (teamProgress.claimReward(echo.id(), sp, currentStage)) {
                            PacketDistributor.sendToPlayer(sp, SyncProgressMessage.forPlayer(teamProgress, sp));
                            PacketDistributor.sendToPlayer(sp, new ClaimRewardResponseMessage(true, Optional.ofNullable(stage.completionRewardSummary())));
                            setDirty();
                        } else {
                            FTBEchoes.LOGGER.error("reward claim failed for echo {}, player {}, stage {}", echo.id(), sp.getGameProfile().name(), stage);
                        }
                    }
                });
                if (completeStage(team, echo)) {
                    notifyTeamCompletion(team, echo, currentStage);
                }
            }
        }
    }

    private void notifyTeamCompletion(Team team, Echo echo, int currentStage) {
        team.getOnlineMembers().forEach(member -> {
            Vec3 vec = member.position();
            Component echoTitle = echo.title().copy().withStyle(ChatFormatting.YELLOW);
            Component stageTitle = echo.stages().get(currentStage).title().copy().withStyle(ChatFormatting.YELLOW);
            member.sendSystemMessage(Component.translatable("ftbechoes.message.echo_stage_complete", echoTitle, stageTitle).withStyle(ChatFormatting.GREEN));
            if (currentStage == echo.stages().size() - 1) {
                member.sendSystemMessage(Component.translatable("ftbechoes.message.echo_complete", echoTitle).withStyle(ChatFormatting.LIGHT_PURPLE));
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

    public boolean resetAllRewards(UUID playerId, Identifier echoId) {
        return applyChange(playerId, progress -> progress.resetAllRewards(echoId, playerId));
    }

    public void consumeLimitedShopPurchase(ServerPlayer player, ShoppingKey key, int count, ShopData shopData) {
        applyChange(player, progress -> {
            progress.consumeShopStock(player, key, count, shopData);
            return true;
        });
    }

    public boolean resetShopStock(ServerPlayer player, Identifier echoId) {
        return applyChange(player, progress -> progress.resetShopStock(echoId));
    }

    public boolean resetShopStock(Team team, Identifier echoId) {
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
        FTBEchoes.LOGGER.debug("applyChange: team='{}' teamId={} getId={} mapSize={}",
                team.getShortName(), team.getTeamId(), team.getId(), progressMap.size());
        TeamProgress teamProgress = progressMap.computeIfAbsent(team.getTeamId(), k -> newProgress());
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
