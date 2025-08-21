package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.block.entity.EchoProjectorBlockEntity;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectEchoMessage(BlockPos projectorPos, ResourceLocation echoId) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, SelectEchoMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SelectEchoMessage::projectorPos,
            ResourceLocation.STREAM_CODEC, SelectEchoMessage::echoId,
            SelectEchoMessage::new
    );

    public static final Type<SelectEchoMessage> TYPE = new Type<>(FTBEchoes.id("select_echo"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(SelectEchoMessage message, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sp
                && sp.hasPermissions(Commands.LEVEL_GAMEMASTERS)
                && sp.level().getBlockEntity(message.projectorPos) instanceof EchoProjectorBlockEntity projector)
        {
            EchoManager.getServerInstance().getEcho(message.echoId)
                    .ifPresent(echo -> projector.setEchoId(echo.id()));
        }
    }
}
