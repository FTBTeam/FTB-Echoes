package dev.ftb.mods.ftbechoes.client;

import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used by JEI (and potentially other recipe mods in future)
 */
public enum ShopSummary {
    INSTANCE;

    private final Int2ObjectMap<List<SummaryItem>> byItemHash = new Int2ObjectOpenHashMap<>();
    private final List<SummaryItem> allShopData = new ArrayList<>();

    public List<SummaryItem> getAllShopData() {
        return Collections.unmodifiableList(allShopData);
    }

    public boolean hasShopData(ItemStack stack) {
        return byItemHash.containsKey(itemKey(stack));
    }

    public List<SummaryItem> getShopDataFor(ItemStack stack) {
        return byItemHash.getOrDefault(itemKey(stack), List.of());
    }

    public void buildSummary() {
        byItemHash.clear();
        allShopData.clear();

        for (Echo echo : EchoManager.getClientInstance().getEchoes()) {
            for (EchoStage stage : echo.stages()) {
                for (ShopData data : stage.shopUnlocked()) {
                    if (!data.stacks().isEmpty()) {
                        SummaryItem summary = new SummaryItem(data, echo.title(), stage.title());
                        for (ItemStack stack : data.stacks()) {
                            int key = itemKey(stack);
                            byItemHash.computeIfAbsent(key, k -> new ArrayList<>()).add(summary);
                        }
                        allShopData.add(summary);
                    }
                }
            }
        }
    }

    private static int itemKey(ItemStack stack) {
        // note: ignoring component data for the purposes of JEI
        return BuiltInRegistries.ITEM.getId(stack.getItem());
    }

    public record SummaryItem(ShopData data, Component echoTitle, Component stageTitle) {
    }
}
