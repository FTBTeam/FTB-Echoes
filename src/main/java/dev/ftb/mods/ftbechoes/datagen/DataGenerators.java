package dev.ftb.mods.ftbechoes.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenerators {
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModLangProvider::new);
        event.createProvider(ModBlockModelProvider::new);
        event.createProvider(ModSoundProvider::new);

        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModLootTableProvider::new);
    }
}
