package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestStageCompletionMessage(ResourceLocation echoId) implements CustomPacketPayload {
    public static final Type<RequestStageCompletionMessage> TYPE = new Type<>(FTBEchoes.id("request_stage_completion"));
    public static final StreamCodec<FriendlyByteBuf, RequestStageCompletionMessage> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, RequestStageCompletionMessage::echoId,
            RequestStageCompletionMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(RequestStageCompletionMessage message, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sp && sp.getServer() != null) {
            FTBTeamsAPI.api().getManager().getTeamForPlayer(sp).ifPresent(team ->
                    EchoManager.getServerInstance().getEcho(message.echoId)
                            .ifPresent(echo -> TeamProgressManager.get(sp.getServer()).tryCompleteStage(sp, team, echo))
            );
        }
    }
}