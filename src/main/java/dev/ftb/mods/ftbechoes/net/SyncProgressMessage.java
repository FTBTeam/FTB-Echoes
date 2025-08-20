package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.ClientProgress;
import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.client.gui.EchoScreen;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncProgressMessage(TeamProgress progress) implements CustomPacketPayload {
    public static final Type<SyncProgressMessage> TYPE = new Type<>(FTBEchoes.id("sync_progress"));

    public static final StreamCodec<FriendlyByteBuf, SyncProgressMessage> STREAM_CODEC = StreamCodec.composite(
            TeamProgress.STREAM_CODEC, SyncProgressMessage::progress,
            SyncProgressMessage::new
    );

    public static SyncProgressMessage forPlayer(TeamProgress progress, ServerPlayer player) {
        return new SyncProgressMessage(progress.forSyncTo(player));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(SyncProgressMessage message, IPayloadContext ignoredContext) {
        ClientProgress.INSTANCE.receiveProgressFromServer(message.progress);
        FTBEchoesClient.onProgressUpdated();
    }
}
