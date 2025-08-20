package dev.ftb.mods.ftbechoes.client;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.gui.EchoScreen;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.shopping.ShoppingBasket;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

@Mod(value = FTBEchoes.MOD_ID, dist = Dist.CLIENT)
public class FTBEchoesClient {
    public FTBEchoesClient(IEventBus modEventBus) {
        modEventBus.addListener(FTBEchoesClient::clientSetup);

        NeoForge.EVENT_BUS.addListener(FTBEchoesClient::playerLoggedIn);
        NeoForge.EVENT_BUS.addListener(FTBEchoesClient::playerLoggedOut);

        StageEntryRenderers.init();
    }

    public static void clientSetup(FMLClientSetupEvent event) {
    }

    private static void playerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        EchoManager.initClient();
    }

    private static void playerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        EchoManager.shutdownClient();
        ShoppingBasket.CLIENT_INSTANCE.clear();
    }

    public static void openEchoScreen(ResourceLocation echoId) {
        FTBEchoes.LOGGER.info("opening screen {}", echoId);

        EchoManager.getClientInstance().getEcho(echoId).ifPresentOrElse(
                echo -> new EchoScreen(echo).openGui(),
                () -> Minecraft.getInstance().player.displayClientMessage(Component.literal("SYNC ERROR: No such echo " + echoId), false)
        );
    }

    public static void onProgressUpdated() {
        EchoScreen screen = ClientUtils.getCurrentGuiAs(EchoScreen.class);
        if (screen != null) {
            screen.onProgressUpdated();
        }
    }

    public static @NotNull MutableComponent formatCost(int cost) {
        return Component.empty().append(Component.literal("⬤ ").withStyle(ChatFormatting.YELLOW)).append(String.valueOf(cost)).withStyle(ChatFormatting.DARK_GREEN);
    }

    public static void notifyError(Component message, Component detail) {
        SimpleToast.error(message, detail);
    }

    public static void notifySuccess(Component message, Component detail) {
        SimpleToast.info(message, detail);
    }
}
