package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.resources.ResourceLocation;

public interface Textures {
    String ICON_LOCATION = FTBEchoes.MOD_ID + ":";
    String GUI_LOCATION = ICON_LOCATION + "icons/";

    ResourceLocation SPEAKER = iconTexture("speaker");
    ResourceLocation SPEAKER_ACTIVE = iconTexture("speaker_active");
    ResourceLocation TASKS = iconTexture("tasks");

    static ResourceLocation iconTexture(String img) {
        return ResourceLocation.parse(GUI_LOCATION + img);
    }
}
