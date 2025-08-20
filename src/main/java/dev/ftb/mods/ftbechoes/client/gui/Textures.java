package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.resources.ResourceLocation;

public interface Textures {
    String ICON_LOCATION = FTBEchoes.MOD_ID + ":";
    String GUI_LOCATION = ICON_LOCATION + "textures/gui/";

    ResourceLocation BASKET = guiTexture("basket.png");

    static ResourceLocation guiTexture(String img) {
        return ResourceLocation.parse(GUI_LOCATION + img);
    }
}
