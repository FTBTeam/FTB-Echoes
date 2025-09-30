package dev.ftb.mods.ftbechoes.integration.jei;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.ShopSummary;
import mezz.jei.api.recipe.RecipeType;

public interface RecipeTypes {
    RecipeType<ShopSummary.SummaryItem> SHOPPING = RecipeType.create(FTBEchoes.MOD_ID, "shopping", ShopSummary.SummaryItem.class);
}
