package dev.ftb.mods.ftbechoes.integration.jei;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import mezz.jei.api.recipe.RecipeType;

public interface RecipeTypes {
    RecipeType<ShopDataSummary> SHOPPING = RecipeType.create(FTBEchoes.MOD_ID, "shopping", ShopDataSummary.class);
}
