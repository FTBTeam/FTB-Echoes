package dev.ftb.mods.ftbechoes.integration.jei;

import dev.ftb.mods.ftbechoes.client.ShopSummary;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;

import java.util.List;

public class ShoppingRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<ShopSummary.SummaryItem> {
    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        return false;
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        return output.getItemStack()
                .map(ShopSummary.INSTANCE::hasShopData)
                .orElse(false);
    }

    @Override
    public List<ShopSummary.SummaryItem> getRecipesForInput(ITypedIngredient<?> input) {
        return List.of();
    }

    @Override
    public List<ShopSummary.SummaryItem> getRecipesForOutput(ITypedIngredient<?> output) {
        return output.getItemStack()
                .map(ShopSummary.INSTANCE::getShopDataFor)
                .orElse(List.of());
    }

    @Override
    public List<ShopSummary.SummaryItem> getAllRecipes() {
        return ShopSummary.INSTANCE.getAllShopData();
    }
}
