package dev.ftb.mods.ftbechoes.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

public enum EchoSoundClipHandler {
    INSTANCE;

    private SoundInstance playingSoundInstance = null;

    public void startPlayingSound(SoundEvent soundEvent) {
        if (playingSoundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(playingSoundInstance);
        }
        playingSoundInstance = createSoundInstance(soundEvent);
        if (playingSoundInstance != null) {
            Minecraft.getInstance().getSoundManager().play(playingSoundInstance);
        }
    }

    public void stopPlayingSound() {
        startPlayingSound(null);
    }

    public boolean isPlayingSound() {
        return playingSoundInstance != null && Minecraft.getInstance().getSoundManager().isActive(playingSoundInstance);
    }

    public boolean isPlayingSound(SoundEvent soundEvent) {
        return isPlayingSound() && playingSoundInstance.getLocation().equals(soundEvent.getLocation());
    }

    @Nullable
    private static SimpleSoundInstance createSoundInstance(SoundEvent sound) {
        if (sound == null) {
            return null;
        }
        return new SimpleSoundInstance(sound.getLocation(), SoundSource.VOICE,
                1f, 1f, SoundInstance.createUnseededRandom(), false, 0,
                SoundInstance.Attenuation.NONE,
                0.0, 0.0, 0.0,
                true
        );
    }
}
