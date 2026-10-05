package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.resources.Identifier;

public interface Textures {
    String ICON_LOCATION = FTBEchoes.MOD_ID + ":";
    String GUI_LOCATION = ICON_LOCATION + "icons/";

    Identifier SPEAKER = iconTexture("speaker");
    Identifier SPEAKER_ACTIVE = iconTexture("speaker_active");
    Identifier TASKS = iconTexture("tasks");
    Identifier STOP_AUDIO = iconTexture("stop_audio");

    static Identifier iconTexture(String img) {
        return Identifier.parse(GUI_LOCATION + img);
    }
}
