package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.client.gui.EchoScreen;
import dev.ftb.mods.ftbechoes.client.gui.ShopPanel;
import dev.ftb.mods.ftbechoes.registry.ModSounds;
import dev.ftb.mods.ftbechoes.shopping.ShoppingBasket;
import dev.ftb.mods.ftbechoes.util.MiscUtil;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
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

            // Tell the shop panel to refresh so any non-dynamic data is updated (e.g. remaining claim limits)
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ScreenWrapper wrapper && wrapper.getGui() instanceof EchoScreen echoScreen) {
                echoScreen.refreshWidgets();
            }
        } else {
            FTBEchoesClient.notifyError(Component.translatable("ftbechoes.message.purchase_failed"), message.message.orElse(Component.empty()));
        }
    }
}
