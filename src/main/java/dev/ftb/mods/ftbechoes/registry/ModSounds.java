package dev.ftb.mods.ftbechoes.registry;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, FTBEchoes.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> COINS = register("coins");

    // some test sounds for audio localization testing
    public static final DeferredHolder<SoundEvent, SoundEvent> THIS_IS_AN_ECHO = register("this_is_an_echo");
    public static final DeferredHolder<SoundEvent, SoundEvent> THIS_IS_AN_ECHO_EN_GB = register("en_gb/this_is_an_echo");
    public static final DeferredHolder<SoundEvent, SoundEvent> THIS_IS_AN_ECHO_FR_FR = register("fr_fr/this_is_an_echo");

    private static DeferredHolder<SoundEvent,SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(FTBEchoes.id(name)));
    }
}
