package dev.ftb.mods.ftbechoes.shopping;

import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache to map shopping keys to the shop entry and the echo stage they come from
 */
public class ShopDataCache {
    private final Map<ShoppingKey, ShoppingEntry> map = new HashMap<>();

    @Nullable
    public ShoppingEntry find(ShoppingKey key) {
        return map.computeIfAbsent(key, k -> EchoManager.getServerInstance().getEcho(key.echoId())
                .map(echo -> findEntry(echo, key.name()))
                .orElse(null));
    }

    private static @Nullable ShoppingEntry findEntry(Echo echo, String name) {
        List<EchoStage> stages = echo.stages();
        for (int stageIdx = 0; stageIdx < stages.size(); stageIdx++) {
            for (ShopData d : stages.get(stageIdx).shopUnlocked()) {
                if (d.name().equals(name)) {
                    return new ShoppingEntry(d, stageIdx);
                }
            }
        }
        return null;
    }

    public void clear() {
        map.clear();
    }

    public record ShoppingEntry(ShopData data, int stageIdx) {
    }
}
