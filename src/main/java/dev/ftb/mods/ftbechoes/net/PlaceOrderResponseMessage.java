package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.MiscUtil;
import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.registry.ModSounds;
import dev.ftb.mods.ftbechoes.shopping.ShoppingBasket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record PlaceOrderResponseMessage(boolean success, Optional<Component> message) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceOrderResponseMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PlaceOrderResponseMessage::success,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, PlaceOrderResponseMessage::message,
            PlaceOrderResponseMessage::new
    );

    public static final Type<PlaceOrderResponseMessage> TYPE = new Type<>(FTBEchoes.id("place_order_response"));

    public static PlaceOrderResponseMessage ok() {
        return new PlaceOrderResponseMessage(true, Optional.empty());
    }

    public static PlaceOrderResponseMessage failed(Component message) {
        return new PlaceOrderResponseMessage(false, Optional.of(message));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(PlaceOrderResponseMessage message, IPayloadContext context) {
        if (message.success) {
            FTBEchoesClient.notifySuccess(
                    Component.translatable("ftbechoes.message.purchase_success"),
                    Component.translatable("ftbechoes.message.purchase_success.2", MiscUtil.formatCost(ShoppingBasket.CLIENT_INSTANCE.getTotalCost()))
            );
            ShoppingBasket.CLIENT_INSTANCE.clear();
            context.player().playSound(ModSounds.COINS.get());
        } else {
            FTBEchoesClient.notifyError(Component.translatable("ftbechoes.message.purchase_failed"), message.message.orElse(Component.empty()));
        }
    }
}
