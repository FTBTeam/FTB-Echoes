package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.client.gui.AudioButtonHolder;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class AudioButton extends SimpleTextButton {
    private static final Icon INACTIVE = Icons.RIGHT;
    private static final Icon ACTIVE = Icons.RIGHT.withBorder(Color4I.rgb(0xFFFF00), false);

    private final SoundEvent sound;
    private SoundInstance playing = null;

    public AudioButton(Panel panel, Component text, SoundEvent sound) {
        super(panel, text, Icons.RIGHT);

        this.sound = sound;
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

    public void startAudio() {
        playing = SimpleSoundInstance.forUI(sound, 1f);
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
