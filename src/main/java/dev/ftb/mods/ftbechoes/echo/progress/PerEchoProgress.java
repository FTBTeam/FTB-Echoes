package dev.ftb.mods.ftbechoes.echo.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.function.Function;

/**
 * Progress for a given team and echo. Note that mutator methods are package-private (only to be called from
 * {@link TeamProgress}).
 */
public final class PerEchoProgress {
    private static final Codec<Map<UUID, Set<Integer>>> REWARDS_CLAIMED
            = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT.listOf().xmap(HashSet::new, ArrayList::new));
    private static final Codec<PerEchoProgress> RAW_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.xmap(MutableInt::new, MutableInt::getValue).fieldOf("current_stage").forGetter(p -> p.currentStage),
            REWARDS_CLAIMED.fieldOf("rewards_claimed").forGetter(p -> p.claimedRewards)
    ).apply(builder, PerEchoProgress::new));
    public static final Codec<PerEchoProgress> CODEC = RAW_CODEC.xmap(
            // ensure claimed rewards map is mutable after loading
            in -> new PerEchoProgress(in.currentStage, new HashMap<>(in.claimedRewards)),
            Function.identity()
    );
    public static final StreamCodec<FriendlyByteBuf, PerEchoProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(MutableInt::new, MutableInt::getValue), p -> p.currentStage,
            ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.INT.apply(ByteBufCodecs.collection(HashSet::new))), p -> p.claimedRewards,
            PerEchoProgress::new
    );

    private final MutableInt currentStage;
    private final Map<UUID, Set<Integer>> claimedRewards;

    /**
     * @param currentStage   last completed stage for this echo (0 indicates team are on first stage, no completions yet)
     * @param claimedRewards the echo rewards claimed by each player on the team, by player UUID
     */
    public PerEchoProgress(MutableInt currentStage, Map<UUID, Set<Integer>> claimedRewards) {
        this.currentStage = currentStage;
        this.claimedRewards = claimedRewards;
    }

    public static PerEchoProgress createEmptyProgress() {
        return new PerEchoProgress(new MutableInt(0), new HashMap<>());
    }

    public int getCurrentStage() {
        return currentStage.intValue();
    }

    void setCurrentStage(int stage) {
        currentStage.setValue(stage);
    }

    public boolean isRewardClaimed(Player player, int stage) {
        return claimedRewards.getOrDefault(player.getUUID(), Set.of()).contains(stage);
    }

    boolean setRewardClaimed(UUID playerId, int stage, boolean claimed) {
        Set<Integer> s = claimedRewards.computeIfAbsent(playerId, k -> new HashSet<>());
        return claimed ? s.add(stage) : s.remove(stage);
    }

    boolean clearRewards(UUID playerId) {
        Set<Integer> s = claimedRewards.computeIfAbsent(playerId, k -> new HashSet<>());
        boolean hadAnyRewards = !s.isEmpty();
        s.clear();
        return hadAnyRewards;
    }

    void completeStage() {
        currentStage.increment();
    }

    public PerEchoProgress forSyncToPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Map<UUID,Set<Integer>> rewards = Map.of(playerId, claimedRewards.getOrDefault(playerId, Set.of()));
        return new PerEchoProgress(new MutableInt(currentStage), rewards);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PerEchoProgress) obj;
        return Objects.equals(this.currentStage, that.currentStage) &&
                Objects.equals(this.claimedRewards, that.claimedRewards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentStage, claimedRewards);
    }

    @Override
    public String toString() {
        return "PerEchoProgress[" +
                "currentStage=" + currentStage + ", " +
                "claimedRewards=" + claimedRewards + ']';
    }
}
