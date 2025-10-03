package dev.ftb.mods.ftbechoes.client;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.block.entity.EchoProjectorBlockEntity;
import dev.ftb.mods.ftbechoes.client.gui.EchoScreen;
import dev.ftb.mods.ftbechoes.client.gui.StageEntryRenderers;
import dev.ftb.mods.ftbechoes.client.render.EchoEntityRenderer;
import dev.ftb.mods.ftbechoes.client.render.EchoProjectorRenderer;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbechoes.registry.ModEntityTypes;
import dev.ftb.mods.ftbechoes.shopping.ShoppingBasket;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@Mod(value = FTBEchoes.MOD_ID, dist = Dist.CLIENT)
public class FTBEchoesClient {
    public FTBEchoesClient(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::playerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::playerLoggedOut);
        NeoForge.EVENT_BUS.addListener(this::clientTick);

        modEventBus.addListener(this::registerRenderers);

        StageEntryRenderers.init();
    }

    private void clientTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!level.isClientSide() || !FTBTeamsAPI.api().isClientManagerLoaded()) {
            return;
        }

        PersistedClientData.get().save();
    }

    private void playerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        EchoManager.initClient();
    }

    private void playerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        EchoManager.shutdownClient();
        ShoppingBasket.CLIENT_INSTANCE.clear();
        PersistedClientData.refreshInstance();
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ECHO_PROJECTOR.get(), EchoProjectorRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ECHO.get(), EchoEntityRenderer::new);
    }

    public static void openEchoScreen(EchoProjectorBlockEntity projector) {
        ResourceLocation echoId = projector.getEchoId();
        BlockPos pos = projector.getBlockPos();
        if (echoId == null) {
            new EchoScreen(pos, null).openGui();
        } else {
            EchoManager.getClientInstance().getEcho(echoId).ifPresentOrElse(
                    echo -> new EchoScreen(pos, echo).openGui(),
                    () -> Minecraft.getInstance().player.displayClientMessage(Component.literal("Unknown echo: " + echoId).withStyle(ChatFormatting.RED), false)
            );
        }
    }

    public static void onProgressUpdated() {
        EchoScreen screen = ClientUtils.getCurrentGuiAs(EchoScreen.class);
        if (screen != null) {
            screen.onProgressUpdated();
        }
    }

    public static void notifyError(Component message, Component detail) {
        SimpleToast.error(message, detail);
    }

    public static void notifySuccess(Component message, Component detail) {
        SimpleToast.info(message, detail);
    }

    public static void onProjectorUpdated(EchoProjectorBlockEntity projector) {
        EchoScreen screen = ClientUtils.getCurrentGuiAs(EchoScreen.class);
        if (screen != null && screen.getProjectorPos().equals(projector.getBlockPos())) {
            EchoManager.getClientInstance().getEcho(projector.getEchoId()).ifPresent(screen::setEcho);
        }
    }
}
