package dev.ftb.mods.ftbechoes.integration.jei;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class FTBEchoesJEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = FTBEchoes.id("default");
    private static IJeiHelpers jeiHelpers;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();

        registration.addRecipeCategories(new ShoppingCategory());
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addTypedRecipeManagerPlugin(RecipeTypes.SHOPPING, new ShoppingRecipeManagerPlugin());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.ECHO_PROJECTOR.asItem(), RecipeTypes.SHOPPING);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    static IGuiHelper guiHelper() {
        return FTBEchoesJEIPlugin.jeiHelpers.getGuiHelper();
    }
}
