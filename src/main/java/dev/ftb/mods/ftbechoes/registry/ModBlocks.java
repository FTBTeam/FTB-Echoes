package dev.ftb.mods.ftbechoes.registry;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.block.EchoProjectorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FTBEchoes.MOD_ID);

    public static Block.Properties defaultProps() {
        return Block.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3f, 10f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 15);
    }

    public static final DeferredBlock<EchoProjectorBlock> ECHO_PROJECTOR
            = BLOCKS.register("echo_projector", () -> new EchoProjectorBlock(defaultProps()));
}
