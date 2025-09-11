package dev.ftb.mods.ftbechoes.echo.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

/**
 * Progress for a given team and echo. The record members are mutable internally, but should only be modified
 * by mutator methods.
 *
 * @param currentStage last completed stage for this echo (0 indicates team are on first stage, no completions yet)
 * @param claimedRewards the echo rewards claimed by each player on the team, by player UUID
 */
public record PerEchoProgress(MutableInt currentStage, Map<UUID, Set<Integer>> claimedRewards) {
    private static final Codec<Map<UUID, Set<Integer>>> REWARDS_CLAIMED
            = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT.listOf().xmap(HashSet::new, ArrayList::new));
//            .xmap(PerEchoProgress::toMutable, PerEchoProgress::convertForSaving);

//    private static Map<UUID, HashSet<Integer>> convertForSaving(Map<UUID, Set<Integer>> m) {
//        HashMap<UUID,HashSet<Integer>> res = new HashMap<>();
//        m.forEach((k, v) -> res.put(k, new HashSet<>(v)));
//        return res;
//    }
//
//    private static Map<UUID, Set<Integer>> toMutable(Map<UUID, HashSet<Integer>> m) {
//        return new HashMap<>(m);
//    }

    public static final Codec<PerEchoProgress> RAW_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.xmap(MutableInt::new, MutableInt::getValue).fieldOf("current_stage").forGetter(p -> p.currentStage),
            REWARDS_CLAIMED.fieldOf("rewards_claimed").forGetter(p -> p.claimedRewards)
    ).apply(builder, PerEchoProgress::new));
    public static final Codec<PerEchoProgress> CODEC = RAW_CODEC.xmap(
            // ensure claimed rewards map is mutable after loading
            in -> new PerEchoProgress(in.currentStage, new HashMap<>(in.claimedRewards)),
            in -> in
    );
    public static final StreamCodec<FriendlyByteBuf, PerEchoProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(MutableInt::new, MutableInt::getValue), PerEchoProgress::currentStage,
            ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.INT.apply(ByteBufCodecs.collection(HashSet::new))), PerEchoProgress::claimedRewards,
            PerEchoProgress::new
    );

    public static PerEchoProgress empty() {
        return new PerEchoProgress(new MutableInt(0), new HashMap<>());
    }

    @Override
    public Map<UUID, Set<Integer>> claimedRewards() {
        return Collections.unmodifiableMap(claimedRewards);
    }

    public int getCurrentStage() {
        return currentStage.intValue();
    }

    public boolean isRewardClaimed(Player player, int stage) {
        return claimedRewards.getOrDefault(player.getUUID(), Set.of()).contains(stage);
    }

    boolean setRewardClaimed(Player player, int stage, boolean claimed) {
        Set<Integer> s = claimedRewards.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        return claimed ? s.add(stage) : s.remove(stage);
    }

    void completeStage() {
        currentStage.increment();
    }

    void setStage(int stage) {
        currentStage.setValue(stage);
    }
}
