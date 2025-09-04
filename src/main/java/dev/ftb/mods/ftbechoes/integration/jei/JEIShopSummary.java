package dev.ftb.mods.ftbechoes.integration.jei;

import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum JEIShopSummary {
    INSTANCE;

    private final Int2ObjectMap<List<ShopDataSummary>> map = new Int2ObjectOpenHashMap<>();
    private final List<ShopDataSummary> allItems = new ArrayList<>();

    public boolean hasShopData(ItemStack stack) {
        return map.containsKey(ItemStack.hashItemAndComponents(stack));
    }

    public List<ShopDataSummary> getAllShopData() {
        return Collections.unmodifiableList(allItems);
    }

    public List<ShopDataSummary> getShopDataFor(ItemStack stack) {
        return map.getOrDefault(ItemStack.hashItemAndComponents(stack), List.of());
    }

    public void buildSummary() {
        map.clear();
        allItems.clear();

        for (Echo echo : EchoManager.getClientInstance().getEchoes()) {
            for (EchoStage stage : echo.stages()) {
                for (ShopData data : stage.shopUnlocked()) {
                    if (!data.stack().isEmpty()) {
                        ShopDataSummary summary = new ShopDataSummary(data, echo.title(), stage.title());
                        allItems.add(summary);
                        int key = ItemStack.hashItemAndComponents(data.stack());
                        map.computeIfAbsent(key, k -> new ArrayList<>()).add(summary);
                    }
                }
            }
        }
    }
}
