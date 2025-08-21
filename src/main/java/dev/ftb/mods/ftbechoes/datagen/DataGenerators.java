package dev.ftb.mods.ftbechoes.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(generator.getPackOutput(), existingFileHelper));
        generator.addProvider(event.includeClient(), new ModLangProvider(generator.getPackOutput()));

        generator.addProvider(event.includeServer(), new ModBlockTagsProvider(generator.getPackOutput(), lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModLootTableProvider(generator.getPackOutput(), lookupProvider));
    }
}
