package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.client.gui.AudioButtonHolder;
import dev.ftb.mods.ftbechoes.client.gui.Textures;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class AudioButton extends SimpleTextButton {
    private static final Icon INACTIVE = Icon.getIcon(Textures.SPEAKER);
    private static final Icon ACTIVE =  Icon.getIcon(Textures.SPEAKER_ACTIVE);

    private final SoundEvent sound;
    private SoundInstance playing = null;

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
        if (playing == null) {
            startAudio();
        } else {
            stopAudio();
        }
    }

    @Override
    public void onClosed() {
       stopAudio();
    }

    @Override
    public void tick() {
        if (playing != null && !Minecraft.getInstance().getSoundManager().isActive(playing)) {
            playing = null;
            setIcon(INACTIVE);
        }
    }

    public void startAudio() {
        playing = SimpleSoundInstance.forUI(sound, 1f, 1f);
        Minecraft.getInstance().getSoundManager().play(playing);
        setIcon(ACTIVE);
        if (parent instanceof AudioButtonHolder h) {
            h.onAudioStart(this);
        }
    }

    public void stopAudio() {
        if (playing != null) {
            Minecraft.getInstance().getSoundManager().stop(playing);
            playing = null;
            setIcon(INACTIVE);
        }
    }
}
