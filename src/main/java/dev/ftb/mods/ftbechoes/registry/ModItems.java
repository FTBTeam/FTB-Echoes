package dev.ftb.mods.ftbechoes.registry;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FTBEchoes.MOD_ID);

    public static final DeferredItem<BlockItem> ECHO_PROJECTOR
            = blockItem("echo_projector", ModBlocks.ECHO_PROJECTOR);

    public static DeferredItem<BlockItem> blockItem(String id, Supplier<? extends Block> sup) {
        return ITEMS.registerSimpleBlockItem(id, sup);
    }
}
