package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.echo.entries.ImageEntry;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import net.minecraft.network.chat.Component;

public class ImageButton extends SimpleButton {
    private final ImageEntry.Alignment alignment;

    public ImageButton(Panel panel, Icon icon, ImageEntry.Alignment alignment) {
        super(panel, Component.empty(), icon, (b, mb) -> {});

        this.alignment = alignment;
    }

    public ImageEntry.Alignment getAlignment() {
        return alignment;
    }

    @Override
    public void playClickSound() {
    }
}
