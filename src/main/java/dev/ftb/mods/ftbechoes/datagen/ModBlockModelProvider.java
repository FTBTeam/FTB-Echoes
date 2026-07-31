package dev.ftb.mods.ftbechoes.datagen;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.registry.ModBlocks;
import dev.ftb.mods.ftbechoes.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class ModBlockModelProvider extends ModelProvider {
    public ModBlockModelProvider(PackOutput packOutput) {
        super(packOutput, FTBEchoes.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block projector = ModBlocks.ECHO_PROJECTOR.get();

        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.SIDE, new Material(Identifier.withDefaultNamespace("block/blackstone")))
                .put(TextureSlot.BOTTOM, new Material(Identifier.withDefaultNamespace("block/blackstone")))
                .put(TextureSlot.TOP, new Material(Identifier.fromNamespaceAndPath(FTBEchoes.MOD_ID, "block/echo_projector_top")));

        var tmpl = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("block/slab")),
                Optional.empty(),
                TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.BOTTOM
        );

        var id = tmpl.create(projector, textures, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(projector, BlockModelGenerators.plainVariant(id)));
        itemModels.itemModelOutput.accept(ModItems.ECHO_PROJECTOR.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(ModBlocks.ECHO_PROJECTOR.get())));
    }

}
