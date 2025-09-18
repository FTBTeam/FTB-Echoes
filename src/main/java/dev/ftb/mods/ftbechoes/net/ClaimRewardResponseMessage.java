package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record ClaimRewardResponseMessage(boolean claimedOK, Optional<Component> msg) implements CustomPacketPayload {
    public static final Type<ClaimRewardResponseMessage> TYPE = new Type<>(FTBEchoes.id("claim_reward_reponse"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClaimRewardResponseMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClaimRewardResponseMessage::claimedOK,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, ClaimRewardResponseMessage::msg,
            ClaimRewardResponseMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(ClaimRewardResponseMessage message, IPayloadContext ignored) {
        Component detail = message.msg.orElse(Component.empty());
        if (message.claimedOK) {
            FTBEchoesClient.notifySuccess(Component.translatable("ftbechoes.message.reward_claimed"), detail);
            FTBEchoesClient.onProgressUpdated();  // refreshes gui if open
        } else {
            FTBEchoesClient.notifyError(Component.translatable("ftbechoes.message.reward_not_claimed"), detail);
        }
    }
}

