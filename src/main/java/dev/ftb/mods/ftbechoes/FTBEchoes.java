package dev.ftb.mods.ftbechoes;

import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.config.FTBEchoesServerConfig;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.net.SyncEchoesMessage;
import dev.ftb.mods.ftbechoes.registry.ModStageEntryTypes;
import dev.ftb.mods.ftbechoes.registry.RegistryKeys;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(FTBEchoes.MOD_ID)
public class FTBEchoes {
    public static final String MOD_ID = "ftbechoes";

    public static final Logger LOGGER = LoggerFactory.getLogger(FTBEchoes.class);

    public FTBEchoes(IEventBus eventBus, ModContainer container) {
        IEventBus forgeBus = NeoForge.EVENT_BUS;

        ConfigManager.getInstance().registerServerConfig(FTBEchoesServerConfig.CONFIG, MOD_ID + ".server_config", false);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            FTBEchoesClient.onModConstruction(eventBus, container);
        }

        eventBus.addListener(this::newRegistries);

        ModStageEntryTypes.STAGE_ENTRY_TYPES.register(eventBus);

        forgeBus.addListener(this::playerLogin);
        forgeBus.addListener(this::registerReloadListeners);
    }

    private void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            EchoManager.getServerInstance().syncToClient(sp);
        }
    }

    private void newRegistries(NewRegistryEvent event) {
        event.register(RegistryKeys.STAGE_ENTRY_REGISTRY);
    }

    private void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new EchoManager.ReloadListener());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
