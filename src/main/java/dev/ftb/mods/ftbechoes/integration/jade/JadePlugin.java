package dev.ftb.mods.ftbechoes.integration.jade;

import dev.ftb.mods.ftbechoes.block.EchoProjectorBlock;
import dev.ftb.mods.ftbechoes.entity.EchoEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(EchoBlockComponent.INSTANCE, EchoProjectorBlock.class);
        registration.registerEntityComponent(EchoEntityComponent.INSTANCE, EchoEntity.class);
    }
}
