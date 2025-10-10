package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record RequestTeamProgressMessage(
        UUID teamId
) implements CustomPacketPayload {
    public static final Type<RequestTeamProgressMessage> TYPE = new Type<>(FTBEchoes.id("request_team_progress"));

    public static final StreamCodec<ByteBuf, RequestTeamProgressMessage> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestTeamProgressMessage::teamId,
            RequestTeamProgressMessage::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(RequestTeamProgressMessage message, IPayloadContext context) {
        Optional<TeamProgress> progress = TeamProgressManager.get().getProgress((ServerPlayer) context.player());
        if (progress.isEmpty()) {
            PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ReturnTeamProgressToScreenMessage(message.teamId, null, List.of()));
            return;
        }

        TeamProgress teamProgress = progress.get();
        var referencedPlayerIds = teamProgress.perEcho().values().stream()
                .flatMap(e -> e.claimedRewards().keySet().stream())
                .toList();

        List<ReturnTeamProgressToScreenMessage.PlayerNameEntry> referencedPlayers = new ArrayList<>();
        for (UUID playerId : referencedPlayerIds) {
           var server = context.player().getServer();
           if (server != null && server.getProfileCache() != null) {
               server.getProfileCache()
                       .get(playerId)
                       .ifPresent(profile -> referencedPlayers.add(new ReturnTeamProgressToScreenMessage.PlayerNameEntry(playerId, Component.literal(profile.getName()))));
           }
        }

        PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ReturnTeamProgressToScreenMessage(message.teamId, teamProgress, referencedPlayers));
    }
}
