package dev.ftb.mods.ftbechoes.integration.jade;

import dev.ftb.mods.ftbechoes.block.EchoProjectorBlock;
import dev.ftb.mods.ftbechoes.entity.EchoEntity;
import snownee.jade.api.*;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(EchoBlockComponent.INSTANCE, EchoProjectorBlock.class);
        registration.registerEntityComponent(EchoEntityComponent.INSTANCE, EchoEntity.class);

        registration.addTooltipCollectedCallback((boxElement, accessor) -> {
            // hide villager health & profession lines for echo projection "villagers"
            if (accessor instanceof EntityAccessor entityAccessor && entityAccessor.getEntity() instanceof EchoEntity) {
                boxElement.getTooltip().remove(JadeIds.MC_ENTITY_HEALTH);
                boxElement.getTooltip().remove(JadeIds.MC_VILLAGER_PROFESSION);
            }
        });
    }
}
