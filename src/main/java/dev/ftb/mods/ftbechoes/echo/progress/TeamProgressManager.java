package dev.ftb.mods.ftbechoes.echo.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.net.SyncProgressMessage;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.function.Function;

public class TeamProgressManager extends SavedData {
    private static final String SAVE_NAME = FTBEchoes.MOD_ID + "_progress";

    // serialization!  using xmap here, so we get mutable hashmaps in the live manager
    private static final Codec<Map<UUID,TeamProgress>> PROGRESS_CODEC
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
        return Util.make(new CompoundTag(), tag ->
                tag.put("progress", CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this)
                        .resultOrPartial(err -> FTBEchoes.LOGGER.error("failed to serialize progress data: {}", err))
                        .orElse(new CompoundTag())));
    }

    public Optional<TeamProgress> getProgress(ServerPlayer sp) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(sp).map(this::getProgress);
    }

    public TeamProgress getProgress(Team team) {
        return get().progressMap.computeIfAbsent(team.getTeamId(), k -> newProgress());
    }

//    public StageStatus getStageStatus(Team team, ResourceLocation echoId, Player player) {
//        Echo echo = EchoManager.getInstance().getEcho(echoId).orElseThrow();
//        TeamProgress progress = getProgress(team);
//        return checkStatus(echo, progress, player, progress.getCurrentStage(echoId));
//    }
//
//    public StageStatus getStageStatus(Team team, ResourceLocation echoId, Player player, int stageToCheck) {
//        Echo echo = EchoManager.getInstance().getEcho(echoId).orElseThrow();
//        TeamProgress progress = getProgress(team);
//        return checkStatus(echo, progress, player, stageToCheck);
//    }
//
//    private StageStatus checkStatus(Echo echo, TeamProgress progress, Player player, int toCheck) {
//        int currentStage = progress.getCurrentStage(echo.id());
//        Validate.isTrue(toCheck >= 0 && toCheck <= echo.stages().size());
//        if (toCheck == echo.stages().size() || toCheck > currentStage) {
//            return StageStatus.COMPLETED;
//        } else {
//            var stage = echo.stages().get(toCheck);
//            return GameStageHelper.hasStage(player, stage.requiredGameStage()) ?
//                    StageStatus.READY_TO_COMPLETE :
//                    StageStatus.IN_PROGRESS;
//        }
//    }

    public boolean completeStage(ServerPlayer player, ResourceLocation echoId) {
        return applyChange(player, progress -> progress.completeStage(echoId));
    }

    public boolean completeStage(Team team, ResourceLocation echoId) {
        return applyChange(team, progress -> progress.completeStage(echoId));
    }

    public boolean claimReward(ServerPlayer player, ResourceLocation echoId, int stage) {
        return applyChange(player, progress -> progress.claimReward(echoId, player, stage));
    }

    public boolean setStage(ServerPlayer player, ResourceLocation echoId, int stage) {
        return applyChange(player, progress -> progress.setStage(echoId, stage));
    }

    public boolean setStage(Team team, ResourceLocation echoId, int stage) {
        return applyChange(team, progress -> progress.setStage(echoId, stage));
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
}
