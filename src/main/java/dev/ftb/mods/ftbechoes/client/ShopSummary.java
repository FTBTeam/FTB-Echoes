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
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
                        for (ItemStackTemplate stack : data.stacks()) {
                            byItemHash.computeIfAbsent(itemKey(stack), _ -> new ArrayList<>()).add(summary);
                        }
                        allShopData.add(summary);
                    }
                }
            }
        }
    }

    private static int itemKey(ItemStack stack) {
        return itemKey(ItemStackTemplate.fromNonEmptyStack(stack));
    }

    private static int itemKey(ItemStackTemplate stack) {
        if (stack.item() == Items.POTION) {
            // special case for potions: take component data into account
            return Objects.hash(stack.item().value(), stack.components());
        } else {
            // other items just care about the item itself
            return BuiltInRegistries.ITEM.getId(stack.item().value());
        }
    }

    public record SummaryItem(ShopData data, Component echoTitle, Component stageTitle) {
    }
}
