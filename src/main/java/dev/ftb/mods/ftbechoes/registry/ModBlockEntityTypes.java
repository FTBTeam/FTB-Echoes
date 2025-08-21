package dev.ftb.mods.ftbechoes.registry;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.block.entity.EchoProjectorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES
            = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FTBEchoes.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EchoProjectorBlockEntity>> ECHO_PROJECTOR
            = BLOCK_ENTITY_TYPES.register("core", () -> BlockEntityType.Builder.of(EchoProjectorBlockEntity::new, ModBlocks.ECHO_PROJECTOR.get()).build(null));
}
