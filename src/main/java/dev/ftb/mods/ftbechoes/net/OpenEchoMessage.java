package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.echo.EchoPage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record OpenEchoMessage(ResourceLocation echoId, Optional<EchoPage> page) implements CustomPacketPayload {
    public static final Type<OpenEchoMessage> TYPE = new Type<>(FTBEchoes.id("open_echo"));

    public static final StreamCodec<FriendlyByteBuf, OpenEchoMessage> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, OpenEchoMessage::echoId,
            ByteBufCodecs.optional(EchoPage.STREAM_CODEC), OpenEchoMessage::page,
            OpenEchoMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(OpenEchoMessage message, IPayloadContext context) {
        context.enqueueWork(() -> FTBEchoesClient.openEchoScreen(message.echoId, null, message.page.orElse(null)));
    }
}
