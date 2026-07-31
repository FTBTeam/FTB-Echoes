package dev.ftb.mods.ftbechoes.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

public enum EchoSoundClipHandler {
    INSTANCE;

    @Nullable
    private SoundInstance playingSoundInstance = null;

    public void startPlayingSound(@Nullable SoundEvent soundEvent) {
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
        //noinspection DataFlowIssue
        return isPlayingSound() && playingSoundInstance.getIdentifier().equals(soundEvent.location());
    }

    @Nullable
    private static SimpleSoundInstance createSoundInstance(@Nullable SoundEvent sound) {
        if (sound == null) {
            return null;
        }
        return new SimpleSoundInstance(sound.location(), SoundSource.VOICE,
                1f, 1f, SoundInstance.createUnseededRandom(), false, 0,
                SoundInstance.Attenuation.NONE,
                0.0, 0.0, 0.0,
                true
        );
    }
}
