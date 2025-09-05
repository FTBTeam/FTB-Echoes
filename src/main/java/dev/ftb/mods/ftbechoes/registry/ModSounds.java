package dev.ftb.mods.ftbechoes.registry;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, FTBEchoes.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> COINS = register("coins");

    private static DeferredHolder<SoundEvent,SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(FTBEchoes.id(name)));
    }

    private static DeferredHolder<SoundEvent,SoundEvent> registerFixed(String name, float range) {
        return SOUNDS.register(name, () -> SoundEvent.createFixedRangeEvent(FTBEchoes.id(name), range));
    }
}
