package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public record ReturnTeamProgressToScreenMessage(
        UUID teamId,
        @Nullable TeamProgress teamProgress,
        Collection<PlayerNameEntry> playerNames
) implements CustomPacketPayload {
    public static final Type<ReturnTeamProgressToScreenMessage> TYPE = new Type<>(FTBEchoes.id("return_team_progress_to_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReturnTeamProgressToScreenMessage> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ReturnTeamProgressToScreenMessage::teamId,
            TeamProgress.STREAM_CODEC, ReturnTeamProgressToScreenMessage::teamProgress,
            PlayerNameEntry.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), ReturnTeamProgressToScreenMessage::playerNames,
            ReturnTeamProgressToScreenMessage::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(ReturnTeamProgressToScreenMessage message, IPayloadContext context) {
        FTBEchoesClient.onTeamProgressProvided(message.teamId(), message.teamProgress(), message.playerNames());
    }

    public record PlayerNameEntry(UUID playerId, Component playerName) {
        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerNameEntry> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, PlayerNameEntry::playerId,
                ComponentSerialization.STREAM_CODEC, PlayerNameEntry::playerName,
                PlayerNameEntry::new
        );
    }
}
