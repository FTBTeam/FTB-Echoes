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
        registrar.playToClient(SyncGameStageMessage.TYPE, SyncGameStageMessage.STREAM_CODEC, SyncGameStageMessage::handleData);
        registrar.playToClient(PlaceOrderResponseMessage.TYPE, PlaceOrderResponseMessage.STREAM_CODEC, PlaceOrderResponseMessage::handleData);
        registrar.playToClient(ClaimRewardResponseMessage.TYPE, ClaimRewardResponseMessage.STREAM_CODEC, ClaimRewardResponseMessage::handleData);
        registrar.playToClient(ReturnTeamProgressToScreenMessage.TYPE, ReturnTeamProgressToScreenMessage.STREAM_CODEC, ReturnTeamProgressToScreenMessage::handleData);
        registrar.playToClient(OpenTeamProgressInfoScreenMessage.TYPE, OpenTeamProgressInfoScreenMessage.STREAM_CODEC, OpenTeamProgressInfoScreenMessage::handleData);

        // serverbound
        registrar.playToServer(RequestStageCompletionMessage.TYPE, RequestStageCompletionMessage.STREAM_CODEC, RequestStageCompletionMessage::handleData);
        registrar.playToServer(PlaceOrderMessage.TYPE, PlaceOrderMessage.STREAM_CODEC, PlaceOrderMessage::handleData);
        registrar.playToServer(SelectEchoMessage.TYPE, SelectEchoMessage.STREAM_CODEC, SelectEchoMessage::handleData);
        registrar.playToServer(ClaimRewardMessage.TYPE, ClaimRewardMessage.STREAM_CODEC, ClaimRewardMessage::handleData);
        registrar.playToServer(RequestTeamProgressMessage.TYPE, RequestTeamProgressMessage.STREAM_CODEC, RequestTeamProgressMessage::handleData);
    }
}
