package dev.ftb.mods.ftbechoes.integration.jade;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.block.entity.EchoProjectorBlockEntity;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EchoBlockComponent implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation ID = FTBEchoes.id("block");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof EchoProjectorBlockEntity be) {
            EchoManager.getClientInstance().getEcho(be.getEchoId()).ifPresent(echo -> iTooltip.add(echo.title()));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
