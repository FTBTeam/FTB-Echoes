package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.client.gui.EchoScreen;
import dev.ftb.mods.ftbechoes.client.gui.Textures;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class AudioButton extends SimpleTextButton {
    private static final Icon INACTIVE = Icon.getIcon(Textures.SPEAKER);
    private static final Icon ACTIVE =  Icon.getIcon(Textures.SPEAKER_ACTIVE);

    private final SoundEvent sound;

    public AudioButton(Panel panel, Component text, SoundEvent sound) {
        super(panel, text, INACTIVE);

        this.sound = sound;
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawPanelBackground(graphics, x + 2, y + 3, w - 4, h - 6);
        GuiHelper.drawHollowRect(graphics, x + 1, y + 2, w - 2, h - 4, theme.getContentColor(WidgetType.DISABLED), true);
    }

    @Override
    public void onClicked(MouseButton mouseButton) {
        EchoScreen screen = ClientUtils.getCurrentGuiAs(EchoScreen.class);
        if (screen != null) {
            if (screen.isPlayingSound(sound)) {
                screen.stopPlayingSound();
            } else {
                screen.startPlayingSound(sound);
            }
        }
    }

    @Override
    public void tick() {
        EchoScreen screen = ClientUtils.getCurrentGuiAs(EchoScreen.class);
        if (screen != null) {
            setIcon(screen.isPlayingSound(sound) ? ACTIVE : INACTIVE);
        }
    }
}
