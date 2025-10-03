package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import dev.ftb.mods.ftbechoes.shopping.ShoppingBasket;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlaceOrderMessage(ShoppingBasket basket) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PlaceOrderMessage> STREAM_CODEC = StreamCodec.composite(
            ShoppingBasket.STREAM_CODEC, PlaceOrderMessage::basket,
            PlaceOrderMessage::new
    );

    public static final Type<PlaceOrderMessage> TYPE = new Type<>(FTBEchoes.id("place_order"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(PlaceOrderMessage message, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sp) {
            ShoppingBasket validatedBasket = message.basket.validate(sp);

            if (FTBEchoes.currencyProvider().takeCurrency(sp, validatedBasket.getTotalCost())) {
                // payment taken, give player the goods
                validatedBasket.giveTo(sp);
                PacketDistributor.sendToPlayer(sp, PlaceOrderResponseMessage.ok());
            } else {
                // already checked clientside, so if we get here, the client is probably lying to us...
                PacketDistributor.sendToPlayer(sp, PlaceOrderResponseMessage.failed(Component.translatable("ftbechoes.tooltip.too_expensive")));
            }
        }
    }
}
