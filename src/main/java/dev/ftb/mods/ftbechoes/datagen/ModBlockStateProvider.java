package dev.ftb.mods.ftbechoes.datagen;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, FTBEchoes.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile projectorModel = models().slab("echo_projector",
                ResourceLocation.withDefaultNamespace("block/blackstone"),
                ResourceLocation.withDefaultNamespace("block/blackstone_top"),
                ResourceLocation.fromNamespaceAndPath(FTBEchoes.MOD_ID, "block/echo_projector_top")
        );
        simpleBlock(ModBlocks.ECHO_PROJECTOR.get(), projectorModel);
        simpleBlockItem(ModBlocks.ECHO_PROJECTOR.get(), projectorModel);
    }

}
