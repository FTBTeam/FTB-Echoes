package dev.ftb.mods.ftbechoes.echo.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Progress for a given team. The record members are mutable internally, but only modified
 * by mutator methods called via {@link TeamProgressManager}.
 *
 * @param perEcho progress on an echo, by echo ID
 */
public record TeamProgress(Map<ResourceLocation, PerEchoProgress> perEcho) {
    private static final Codec<Map<ResourceLocation,PerEchoProgress>> ECHO_STAGE
            = Codec.unboundedMap(ResourceLocation.CODEC, PerEchoProgress.CODEC).xmap(HashMap::new, Map::copyOf);

    public static final Codec<TeamProgress> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ECHO_STAGE.fieldOf("per_echo").forGetter(p -> p.perEcho)
    ).apply(builder, TeamProgress::new));
    public static final StreamCodec<FriendlyByteBuf, TeamProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, PerEchoProgress.STREAM_CODEC), TeamProgress::perEcho,
            TeamProgress::new
    );

    public static TeamProgress createNew() {
        return new TeamProgress(new HashMap<>());
    }

    public TeamProgress forSyncTo(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Map<ResourceLocation, PerEchoProgress> map = new HashMap<>();
        perEcho.forEach((id, rec) -> {
            Map<UUID,Set<Integer>> rewards = Map.of(playerId, rec.claimedRewards().getOrDefault(playerId, Set.of()));
            map.put(id, new PerEchoProgress(rec.currentStage(), rewards));
        });

        return new TeamProgress(map);
    }

    @Override
    public Map<ResourceLocation, PerEchoProgress> perEcho() {
        return Collections.unmodifiableMap(perEcho);
    }

    public boolean isEmpty() {
        return perEcho.isEmpty();
    }

    public boolean isRewardClaimed(ResourceLocation echoId, Player player, int stage) throws IllegalArgumentException {
        return getPerEchoProgress(echoId).isRewardClaimed(player, stage);
    }

    public int getCurrentStage(ResourceLocation echoId) throws IllegalArgumentException {
        return getPerEchoProgress(echoId).getCurrentStage();
    }

    public boolean isStageAvailable(ResourceLocation id, int stageIdx) {
        return stageIdx >= getCurrentStage(id);
    }

    boolean claimReward(ResourceLocation echoId, Player player, int stage) {
        return getPerEchoProgress(echoId).setRewardClaimed(player, stage);
    }

    boolean completeStage(ResourceLocation echoId) {
        PerEchoProgress per = getPerEchoProgress(echoId);
        var echo = EchoManager.getServerInstance().getEcho(echoId).orElseThrow();
        if (per.getCurrentStage() < echo.stages().size()) {
            per.completeStage();
            return true;
        }
        return false;
    }

    public Boolean setStage(ResourceLocation echoId, int stage) {
        PerEchoProgress per = getPerEchoProgress(echoId);
        var echo = EchoManager.getServerInstance().getEcho(echoId).orElseThrow();
        per.setStage(Mth.clamp(stage, 0, echo.stages().size() - 1));
        return true;
    }

    @NotNull
    private PerEchoProgress getPerEchoProgress(ResourceLocation echoId) throws IllegalArgumentException {
        return perEcho.computeIfAbsent(echoId, k -> PerEchoProgress.empty());
    }
}
