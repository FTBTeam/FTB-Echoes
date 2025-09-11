package dev.ftb.mods.ftbechoes.shopping;

import com.google.common.collect.Maps;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Tracking what player currently has on order from the shop. Also sent to the server by PlaceOrderMessage.
 */
public class ShoppingBasket {
    public static final StreamCodec<FriendlyByteBuf, ShoppingBasket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ShoppingKey.STREAM_CODEC, ByteBufCodecs.VAR_INT), b -> b.orders,
            ShoppingBasket::new
    );

    private final Object2IntMap<ShoppingKey> orders;
    private int totalCost = 0;

    public static final ShoppingBasket CLIENT_INSTANCE = new ShoppingBasket(Map.of());

    private ShoppingBasket(Map<ShoppingKey, Integer> map) {
        orders = new Object2IntOpenHashMap<>(map);
        recalc();
    }

    public void adjust(ShoppingKey key, int count, int max) {
        int newCount = orders.getOrDefault(key, 0) + count;
        if (newCount <= 0) {
            orders.removeInt(key);
        } else {
            orders.put(key, Math.min(max, newCount));
        }
        recalc();
    }

    public int get(ShoppingKey key) {
        return orders.getOrDefault(key, 0);
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public void clear() {
        orders.clear();
        recalc();
    }

    public void forEach(BiConsumer<ShoppingKey,Integer> consumer) {
        orders.forEach(consumer);
    }

    public int getTotalCost() {
        return totalCost;
    }

    public void giveTo(ServerPlayer player) {
        EchoManager mgr = EchoManager.getServerInstance();
        orders.forEach((key, nOrders) -> mgr.getShopData(key).ifPresent(data -> data.giveTo(player, nOrders)));
    }

    /**
     * Perform server-side validation of a basket's contents, ensuring only items the player actually has access
     * to (based on their current echo progress) are included.
     *
     * @param player the player to check
     * @return a new basket, including only items the player has unlocked
     */
    public ShoppingBasket validate(ServerPlayer player) {
        Map<ShoppingKey,Integer> map = new HashMap<>();

        if (player.getServer() != null) {
            TeamProgressManager.get(player.getServer()).getProgress(player).ifPresent(progress -> {
                EchoManager mgr = EchoManager.getServerInstance();
                orders.forEach((key, amount) -> mgr.getShoppingEntry(key).ifPresent(entry -> {
                    if (progress.isStageCompleted(key.echoId(), entry.stageIdx())) {
                        map.put(key, amount);
                    }
                }));
            });
        }

        return new ShoppingBasket(map);
    }

    private void recalc() {
        totalCost = 0;
        EchoManager mgr = this == CLIENT_INSTANCE ? EchoManager.getClientInstance() : EchoManager.getServerInstance();
        orders.forEach((key, nOrders) ->
                totalCost += mgr.getShopData(key).map(data -> data.cost() * nOrders).orElse(0)
        );
    }
}
