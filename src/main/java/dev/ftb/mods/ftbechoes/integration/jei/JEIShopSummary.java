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

    private final Int2ObjectMap<List<ShopDataSummary>> byItemHash = new Int2ObjectOpenHashMap<>();
    private final List<ShopDataSummary> allShopData = new ArrayList<>();

    public List<ShopDataSummary> getAllShopData() {
        return Collections.unmodifiableList(allShopData);
    }

    public boolean hasShopData(ItemStack stack) {
        return byItemHash.containsKey(ItemStack.hashItemAndComponents(stack));
    }

    public List<ShopDataSummary> getShopDataFor(ItemStack stack) {
        return byItemHash.getOrDefault(ItemStack.hashItemAndComponents(stack), List.of());
    }

    public void buildSummary() {
        byItemHash.clear();
        allShopData.clear();

        for (Echo echo : EchoManager.getClientInstance().getEchoes()) {
            for (EchoStage stage : echo.stages()) {
                for (ShopData data : stage.shopUnlocked()) {
                    if (!data.stacks().isEmpty()) {
                        ShopDataSummary summary = new ShopDataSummary(data, echo.title(), stage.title());
                        for (ItemStack stack : data.stacks()) {
                            int key = ItemStack.hashItemAndComponents(stack);
                            byItemHash.computeIfAbsent(key, k -> new ArrayList<>()).add(summary);
                        }
                        allShopData.add(summary);
                    }
                }
            }
        }
    }
}
