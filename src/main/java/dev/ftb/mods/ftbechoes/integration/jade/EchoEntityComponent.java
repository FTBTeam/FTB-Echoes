package dev.ftb.mods.ftbechoes.integration.jade;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.entity.EchoEntity;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EchoEntityComponent implements IEntityComponentProvider {
    INSTANCE;

    private static final Identifier ID = FTBEchoes.id("entity");

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if (entityAccessor.getEntity() instanceof EchoEntity e) {
            EchoManager.getClientInstance().getEcho(e.getEchoId()).ifPresent(echo -> iTooltip.add(echo.title()));
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}
