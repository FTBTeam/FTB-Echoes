package dev.ftb.mods.ftbechoes.integration.jei;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static dev.ftb.mods.ftbechoes.integration.jei.FTBEchoesJEIPlugin.guiHelper;

public class ShoppingCategory implements IRecipeCategory<ShopDataSummary> {
    private static final ResourceLocation BG_TEXTURE = FTBEchoes.id("textures/gui/jei_shopping.png");
    public static final ResourceLocation MONEY_BAG = ResourceLocation.fromNamespaceAndPath("ftblibrary", "textures/icons/money_bag.png");

    private final IDrawable background;
    private final IDrawable moneyIcon;

    public ShoppingCategory() {
        background = guiHelper().drawableBuilder(BG_TEXTURE, 0, 0, 64, 32)
                .setTextureSize(64, 32)
                .build();
        moneyIcon = guiHelper().drawableBuilder(MONEY_BAG, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public RecipeType<ShopDataSummary> getRecipeType() {
        return RecipeTypes.SHOPPING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ftbechoes.jei.shop.title");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return guiHelper().createDrawableItemStack(ModBlocks.ECHO_PROJECTOR.toStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ShopDataSummary recipe, IFocusGroup focuses) {
        builder.addOutputSlot(42, 8).addItemStack(recipe.data().stack());
    }

    @Override
    public void draw(ShopDataSummary recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);

        moneyIcon.draw(guiGraphics, 5, 8);

        String costStr = String.valueOf(recipe.data().cost());
        Font font = Minecraft.getInstance().font;
        int w = font.width(costStr);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(4f + (18 - w) / 2f, 15f, 0);
        guiGraphics.drawString(font, costStr, -1, 0, 0, false);
        guiGraphics.drawString(font, costStr,  1, 0, 0, false);
        guiGraphics.drawString(font, costStr, 0, -1, 0, false);
        guiGraphics.drawString(font, costStr, 0,  1, 0, false);
        guiGraphics.drawString(font, costStr, 0, 0, 0xFFE0E000, false);
        guiGraphics.pose().popPose();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ShopDataSummary recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(recipe.data().stack().getHoverName().copy().withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
        recipe.data().description().ifPresent(tooltip::add);
        tooltip.add(Component.translatable("ftbechoes.jei.echo_title", recipe.echoTitle()).withStyle(ChatFormatting.GRAY));
        recipe.stageTitle().ifPresent(c -> tooltip.add(Component.translatable("ftbechoes.jei.stage_title", c).withStyle(ChatFormatting.GRAY)));
    }

    @Override
    public int getWidth() {
        return 64;
    }

    @Override
    public int getHeight() {
        return 32;
    }
}
