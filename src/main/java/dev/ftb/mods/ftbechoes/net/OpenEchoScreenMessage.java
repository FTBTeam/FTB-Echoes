package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenEchoScreenMessage(ResourceLocation echoId) implements CustomPacketPayload {
    public static final Type<OpenEchoScreenMessage> TYPE = new Type<>(FTBEchoes.id("open_echo_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenEchoScreenMessage> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, OpenEchoScreenMessage::echoId,
            OpenEchoScreenMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(OpenEchoScreenMessage message, IPayloadContext context) {
        FTBEchoesClient.openEchoScreen(message.echoId);
    }
}
