package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClaimRewardMessage(ResourceLocation echoId, int stageIdx) implements CustomPacketPayload {
    public static final Type<ClaimRewardMessage> TYPE = new Type<>(FTBEchoes.id("claim_reward"));

    public static final StreamCodec<FriendlyByteBuf, ClaimRewardMessage> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ClaimRewardMessage::echoId,
            ByteBufCodecs.VAR_INT, ClaimRewardMessage::stageIdx,
            ClaimRewardMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(ClaimRewardMessage message, IPayloadContext context) {
        Player player = context.player();
        boolean claimedOK = false;
        if (player instanceof ServerPlayer sp && sp.getServer() != null) {
            if (EchoManager.getServerInstance().isKnownEcho(message.echoId)) {
                TeamProgressManager mgr = TeamProgressManager.get(sp.getServer());
                var progress = mgr.getProgress(sp).orElse(TeamProgress.NONE);
                if (progress.getCurrentStage(message.echoId) > message.stageIdx && !progress.isRewardClaimed(message.echoId, sp, message.stageIdx)) {
                    claimedOK = mgr.claimReward(sp, message.echoId, message.stageIdx);
                }
                PacketDistributor.sendToPlayer(sp, SyncProgressMessage.forPlayer(progress, sp));
            }
            PacketDistributor.sendToPlayer(sp, new ClaimRewardResponseMessage(claimedOK));
        }
    }
}
