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
        registrar.playToClient(SyncProgressMessage.TYPE, SyncProgressMessage.STREAM_CODEC, SyncProgressMessage::handleData);
        registrar.playToClient(OpenEchoScreenMessage.TYPE, OpenEchoScreenMessage.STREAM_CODEC, OpenEchoScreenMessage::handleData);
        registrar.playToClient(SyncGameStageMessage.TYPE, SyncGameStageMessage.STREAM_CODEC, SyncGameStageMessage::handleData);
        registrar.playToClient(PlaceOrderResponseMessage.TYPE, PlaceOrderResponseMessage.STREAM_CODEC, PlaceOrderResponseMessage::handleData);

        // serverbound
        registrar.playToServer(RequestStageCompletionMessage.TYPE, RequestStageCompletionMessage.STREAM_CODEC, RequestStageCompletionMessage::handleData);
        registrar.playToServer(PlaceOrderMessage.TYPE, PlaceOrderMessage.STREAM_CODEC, PlaceOrderMessage::handleData);
    }
}
