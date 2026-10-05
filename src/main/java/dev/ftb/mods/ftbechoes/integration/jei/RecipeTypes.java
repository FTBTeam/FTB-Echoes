package dev.ftb.mods.ftbechoes.integration.jei;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.ShopSummary;
import mezz.jei.api.recipe.types.IRecipeType;

public interface RecipeTypes {
    IRecipeType<ShopSummary.SummaryItem> SHOPPING = IRecipeType.create(FTBEchoes.MOD_ID, "shopping", ShopSummary.SummaryItem.class);
}
