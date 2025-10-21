package dev.ftb.mods.ftbechoes.integration.quests;

import dev.ftb.mods.ftbechoes.shopping.ShopData;
import dev.ftb.mods.ftbquests.item.LootCrateItem;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class FTBQuestsIntegration {
    public static List<Component> getLootData(ShopData data) {
        boolean foundLootItem = false;
        List<Component> components = new ArrayList<>();

        var stacks = data.stacks();
        for (var stack : stacks) {
            var item = stack.getItem();
            if (item instanceof LootCrateItem) {
                LootCrate crate = LootCrateItem.getCrate(stack, true);
                if (crate == null) {
                    continue;
                }

                if (foundLootItem) {
                    components.add(Component.literal("")); // blank line between multiple loot crates
                }

                foundLootItem = true;
                RewardTable table = crate.getTable();
                float totalWeight = table.getTotalWeight(true);

                // Sorted by chance with the highest chance first
                List<WeightedReward> weightedRewards = table.getWeightedRewards()
                        .stream()
                        .sorted((a, b) -> {
                            float aChance = a.getWeight() / totalWeight;
                            float bChance = b.getWeight() / totalWeight;

                            return Float.compare(bChance, aChance);
                        })
                        .toList();

                for (var weightedReward : weightedRewards) {
                    Reward reward = weightedReward.getReward();
                    float weight = weightedReward.getWeight();

                    components.add(reward.getTitle().copy().append(Component.literal(" [" + WeightedReward.chanceString(weight, totalWeight) + "]").withStyle(ChatFormatting.DARK_GRAY)));
                }
            }
        }

        if (foundLootItem) {
            return components;
        }

        return null;
    }
}
