package dev.ftb.mods.ftbechoes.client;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.block.entity.EchoProjectorBlockEntity;
import dev.ftb.mods.ftbechoes.client.gui.EchoScreen;
import dev.ftb.mods.ftbechoes.client.gui.EchoSoundClipHandler;
import dev.ftb.mods.ftbechoes.client.gui.StageEntryRenderers;
import dev.ftb.mods.ftbechoes.client.render.EchoEntityRenderer;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.EchoPage;
import dev.ftb.mods.ftbechoes.registry.ModEntityTypes;
import dev.ftb.mods.ftbechoes.shopping.ShoppingBasket;
import dev.ftb.mods.ftblibrary.client.gui.SimpleToast;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

@Mod(value = FTBEchoes.MOD_ID, dist = Dist.CLIENT)
public class FTBEchoesClient {
    private int altKeyTime = 0;

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

        if (Minecraft.getInstance().hasAltDown()) {
            altKeyTime++;
            if (altKeyTime >= 40 && EchoSoundClipHandler.INSTANCE.isPlayingSound()) {
                EchoSoundClipHandler.INSTANCE.stopPlayingSound();
                ClientUtils.getClientPlayer().playSound(SoundEvents.COMPARATOR_CLICK, 0.7f, 0.5f);
            }
        } else {
            altKeyTime = 0;
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
        event.registerEntityRenderer(ModEntityTypes.ECHO.get(), EchoEntityRenderer::new);
    }

    public static void openEchoScreenForProjector(EchoProjectorBlockEntity projector) {
        Identifier echoId = projector.getEchoId();
        BlockPos pos = projector.getBlockPos();
        if (echoId == null) {
            new EchoScreen(pos, null).openGui();
        } else {
            openEchoScreen(echoId, pos, null);
        }
    }

    public static void openEchoScreen(Identifier echoId, @Nullable BlockPos pos, @Nullable EchoPage switchToPage) {
        EchoManager.getClientInstance().getEcho(echoId).ifPresentOrElse(
                echo -> {
                    var screen = new EchoScreen(pos, echo);
                    screen.openGui();
                    if (switchToPage != null) {
                        screen.setCurrentPage(switchToPage);
                    }
                },
                () -> ClientUtils.getClientPlayer().sendSystemMessage(Component.translatable("ftbechoes.commands.unknown_echo", echoId).withStyle(ChatFormatting.RED))
        );
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
        if (screen != null && screen.getProjectorPos() != null && screen.getProjectorPos().equals(projector.getBlockPos())) {
            EchoManager.getClientInstance().getEcho(projector.getEchoId()).ifPresent(screen::setEcho);
        }
    }
}
