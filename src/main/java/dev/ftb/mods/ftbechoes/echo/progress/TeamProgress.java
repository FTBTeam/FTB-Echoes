package dev.ftb.mods.ftbechoes.echo.progress;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import dev.ftb.mods.ftbechoes.shopping.ShoppingKey;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * Progress for a given team. The record members are mutable internally, but only modified
 * by mutator methods called via {@link TeamProgressManager}.
 *
 * @param perEcho progress on an echo, by echo ID
 */
public record TeamProgress(Map<ResourceLocation, PerEchoProgress> perEcho, Map<ShoppingKey, Integer> limitedShopPurchases) {
    public static final TeamProgress NONE = new TeamProgress(Map.of(), Map.of());

    private static final Codec<Map<ResourceLocation,PerEchoProgress>> ECHO_STAGE
            = Codec.unboundedMap(ResourceLocation.CODEC, PerEchoProgress.CODEC).xmap(HashMap::new, Map::copyOf);
    public static final Codec<Map<ShoppingKey, Integer>> LIMITED_PURCHASE_CODEC
            = Codec.list(Codec.pair(ShoppingKey.CODEC.fieldOf("key").codec(), Codec.INT.fieldOf("count").codec()))
            .xmap(TeamProgress::toMap, TeamProgress::toList);

    public static final Codec<TeamProgress> RAW_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ECHO_STAGE.fieldOf("per_echo").forGetter(TeamProgress::perEcho),
            LIMITED_PURCHASE_CODEC.fieldOf("limited_shop_purchases").forGetter(TeamProgress::limitedShopPurchases)
    ).apply(builder, TeamProgress::new));

    public static final Codec<TeamProgress> CODEC = RAW_CODEC.xmap(
            // ensure maps are mutable after loading
            in -> new TeamProgress(new HashMap<>(in.perEcho), new HashMap<>(in.limitedShopPurchases)),
            Function.identity()
    );

    public static final StreamCodec<FriendlyByteBuf, TeamProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, PerEchoProgress.STREAM_CODEC), TeamProgress::perEcho,
            ByteBufCodecs.map(HashMap::new, ShoppingKey.STREAM_CODEC, ByteBufCodecs.INT), p -> p.limitedShopPurchases,
            TeamProgress::new
    );

    private static <K,V> Map<K,V> toMap(List<Pair<K,V>> list) {
        return Util.make(new HashMap<>(), map -> list.forEach(pair -> map.put(pair.getFirst(), pair.getSecond())));
    }

    private static <K,V> List<Pair<K,V>> toList(Map<K,V> map) {
        return Util.make(new ArrayList<>(), list -> map.forEach((k,v) -> list.add(Pair.of(k, v))));
    }

    public static TeamProgress createNew() {
        return new TeamProgress(new HashMap<>(), new HashMap<>());
    }

    public TeamProgress forSyncTo(ServerPlayer player) {
        Map<ResourceLocation, PerEchoProgress> map = new HashMap<>();
        perEcho.forEach((id, rec) -> map.put(id, rec.forSyncToPlayer(player)));
        return new TeamProgress(map, limitedShopPurchases);
    }

    @Override
    public Map<ResourceLocation, PerEchoProgress> perEcho() {
        return Collections.unmodifiableMap(perEcho);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isRewardClaimed(ResourceLocation echoId, Player player, int stage) {
        return getPerEchoProgress(echoId).isRewardClaimed(player, stage);
    }

    public int getCurrentStage(ResourceLocation echoId) {
        return getPerEchoProgress(echoId).getCurrentStage();
    }

    public boolean isStageCompleted(ResourceLocation id, int stageIdx) {
        return stageIdx < getCurrentStage(id);
    }

    /****************************************************************************************
     * mutator methods below here are package-private and only called via TeamProgressManager
     */

    void consumeLimitedShopPurchase(ShoppingKey key, int count) {
        limitedShopPurchases.merge(key, count, Integer::sum);
    }

    boolean resetAllRewards(ResourceLocation echoId, UUID playerId) {
        return getPerEchoProgress(echoId).clearRewards(playerId);
    }

    boolean resetReward(ResourceLocation echoId, UUID playerId, int stageIdx) {
        return getPerEchoProgress(echoId).setRewardClaimed(playerId, stageIdx, false);
    }

    boolean claimReward(ResourceLocation echoId, ServerPlayer player, int stage) {
        return EchoManager.getServerInstance().getEcho(echoId).map(echo -> {
            if (stage >= 0 && stage < echo.stages().size()) {
                echo.stages().get(stage).completionReward().ifPresent(c -> c.giveToPlayer(player));
                return getPerEchoProgress(echoId).setRewardClaimed(player.getUUID(), stage, true);
            }
            return false;
        }).orElse(false);
    }

    boolean completeStage(Echo echo) {
        PerEchoProgress per = getPerEchoProgress(echo.id());
        if (per.getCurrentStage() < echo.stages().size()) {
            per.completeStage();
            return true;
        }
        return false;
    }

    boolean setStage(ResourceLocation echoId, int stageIdx) {
        PerEchoProgress per = getPerEchoProgress(echoId);
        var echo = EchoManager.getServerInstance().getEcho(echoId).orElseThrow();
        per.setCurrentStage(Mth.clamp(stageIdx, 0, echo.stages().size() - 1));
        return true;
    }

    @NotNull
    private PerEchoProgress getPerEchoProgress(ResourceLocation echoId) {
        return perEcho.computeIfAbsent(echoId, k -> PerEchoProgress.createEmptyProgress());
    }

    public int getRemainingLimitedShopPurchases(ShoppingKey key, ShopData data) {
        return data.maxClaims().orElse(0) - limitedShopPurchases.getOrDefault(key, 0);
    }
}
