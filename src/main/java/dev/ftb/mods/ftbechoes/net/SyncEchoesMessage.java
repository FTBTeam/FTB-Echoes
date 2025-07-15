package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Collection;

public record SyncEchoesMessage(Collection<Echo> echoes) implements CustomPacketPayload {
    public static final Type<SyncEchoesMessage> TYPE = new Type<>(FTBEchoes.id("sync_echoes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEchoesMessage> STREAM_CODEC = StreamCodec.composite(
            Echo.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), SyncEchoesMessage::echoes,
            SyncEchoesMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(SyncEchoesMessage message, IPayloadContext context) {
        EchoManager.getClientInstance().syncFromServer(message.echoes);
    }
}
