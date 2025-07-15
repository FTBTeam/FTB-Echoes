package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = FTBEchoes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class FTBEchoesNet {
    private static final String NETWORK_VERSION = "1.0";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(FTBEchoes.MOD_ID)
                .versioned(NETWORK_VERSION);

        // clientbound
        registrar.playToClient(SyncEchoesMessage.TYPE, SyncEchoesMessage.STREAM_CODEC, SyncEchoesMessage::handleData);
    }
}
