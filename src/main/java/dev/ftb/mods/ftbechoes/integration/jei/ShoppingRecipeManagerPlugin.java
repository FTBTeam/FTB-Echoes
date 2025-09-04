package dev.ftb.mods.ftbechoes.integration.jei;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;

import java.util.List;

public class ShoppingRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<ShopDataSummary> {
    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        return false;
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        return output.getItemStack()
                .map(JEIShopSummary.INSTANCE::hasShopData)
                .orElse(false);
    }

    @Override
    public List<ShopDataSummary> getRecipesForInput(ITypedIngredient<?> input) {
        return List.of();
    }

    @Override
    public List<ShopDataSummary> getRecipesForOutput(ITypedIngredient<?> output) {
        return output.getItemStack()
                .map(JEIShopSummary.INSTANCE::getShopDataFor)
                .orElse(List.of());
    }

    @Override
    public List<ShopDataSummary> getAllRecipes() {
        return JEIShopSummary.INSTANCE.getAllShopData();
    }
}
