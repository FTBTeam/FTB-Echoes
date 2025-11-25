package dev.ftb.mods.ftbechoes.datagen;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundProvider extends SoundDefinitionsProvider {
    protected ModSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, FTBEchoes.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.COINS, SoundDefinition.definition()
                .with(sound(FTBEchoes.id("coins")))
                .subtitle("ftbechoes.subtitle.coins_clink"));

        add(ModSounds.THIS_IS_AN_ECHO, SoundDefinition.definition()
                .with(sound(FTBEchoes.id("this_is_an_echo"))));
        add(ModSounds.THIS_IS_AN_ECHO_EN_GB, SoundDefinition.definition()
                .with(sound(FTBEchoes.id("en_gb/this_is_an_echo"))));
        add(ModSounds.THIS_IS_AN_ECHO_FR_FR, SoundDefinition.definition()
                .with(sound(FTBEchoes.id("fr_fr/this_is_an_echo"))));
    }
}
