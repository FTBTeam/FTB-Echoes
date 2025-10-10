package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.gui.EchoProgressInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenTeamProgressInfoScreenMessage() implements CustomPacketPayload {
    public static final Type<OpenTeamProgressInfoScreenMessage> TYPE = new Type<>(FTBEchoes.id("open_team_progress_info_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTeamProgressInfoScreenMessage> STREAM_CODEC = StreamCodec.unit(new OpenTeamProgressInfoScreenMessage());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(OpenTeamProgressInfoScreenMessage message, IPayloadContext context) {
        new EchoProgressInfo().openGuiLater();
    }
}
