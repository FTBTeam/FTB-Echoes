package dev.ftb.mods.ftbechoes;

import dev.ftb.mods.ftbechoes.command.FTBEchoesCommands;
import dev.ftb.mods.ftbechoes.config.FTBEchoesServerConfig;
import dev.ftb.mods.ftbechoes.datagen.DataGenerators;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import dev.ftb.mods.ftbechoes.integration.magic_coins.MagicCoinsCurrency;
import dev.ftb.mods.ftbechoes.net.OpenEchoScreenMessage;
import dev.ftb.mods.ftbechoes.net.SyncGameStageMessage;
import dev.ftb.mods.ftbechoes.net.SyncProgressMessage;
import dev.ftb.mods.ftbechoes.registry.ModArgumentTypes;
import dev.ftb.mods.ftbechoes.registry.ModStageEntryTypes;
import dev.ftb.mods.ftbechoes.registry.RegistryKeys;
import dev.ftb.mods.ftbechoes.shopping.CurrencyPlugin;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftbteams.api.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Mod(FTBEchoes.MOD_ID)
public class FTBEchoes {
    public static final String MOD_ID = "ftbechoes";

    public static final Logger LOGGER = LoggerFactory.getLogger(FTBEchoes.class);

    public static final CurrencyPlugin currencyPlugin = MagicCoinsCurrency.INSTANCE;

    public FTBEchoes(IEventBus eventBus, ModContainer container) {
        IEventBus forgeBus = NeoForge.EVENT_BUS;

        ConfigManager.getInstance().registerServerConfig(FTBEchoesServerConfig.CONFIG, MOD_ID + ".server_config", false);

        eventBus.addListener(DataGenerators::gatherData);
        eventBus.addListener(this::onNewRegistry);

        registerAll(eventBus);

        forgeBus.addListener(this::onServerAboutToStart);
        forgeBus.addListener(this::onServerStopped);
        forgeBus.addListener(this::onPlayerLogin);
        forgeBus.addListener(this::registerReloadListeners);
        forgeBus.addListener(this::onPlayerInteract);
        forgeBus.addListener(FTBEchoesCommands::registerCommands);

        TeamEvent.PLAYER_LOGGED_IN.register(this::onPlayerTeamLogin);
    }

    private static void registerAll(IEventBus eventBus) {
        ModStageEntryTypes.STAGE_ENTRY_TYPES.register(eventBus);
        ModArgumentTypes.COMMAND_ARGUMENT_TYPES.register(eventBus);
    }

    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        EchoManager.initServer();
    }

    private void onServerStopped(ServerStoppedEvent event) {
        EchoManager.shutdownServer();
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            EchoManager.getServerInstance().syncToClient(sp);
        }
    }

    private void onPlayerTeamLogin(PlayerLoggedInAfterTeamEvent event) {
        var player = event.getPlayer();
        var server = Objects.requireNonNull(player.getServer());
        var progress = TeamProgressManager.get(server).getProgress(event.getTeam());
        if (!progress.isEmpty()) {
            PacketDistributor.sendToPlayer(player, SyncProgressMessage.forPlayer(progress, player));
            PacketDistributor.sendToPlayer(player, SyncGameStageMessage.add(player.getTags()));
        }
    }

    private void onNewRegistry(NewRegistryEvent event) {
        event.register(RegistryKeys.STAGE_ENTRY_REGISTRY);
    }

    private void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        // TODO temporary event handler until echo entities are implemented
        BlockPos pos = event.getHitVec().getBlockPos();
        if (event.getEntity() instanceof ServerPlayer sp && sp.getMainHandItem().getItem() == Items.STICK && event.getLevel().getBlockEntity(pos) instanceof SignBlockEntity sign) {
            var text = sign.getFrontText();
            if (text.getMessage(0, false).getString().equals("echo")) {
                String namespace = text.getMessage(1, false).getString();
                String path = text.getMessage(2, false).getString();
                if (!namespace.isEmpty() && !path.isEmpty()) {
                    try {
                        var echoId = ResourceLocation.fromNamespaceAndPath(namespace, path);
                        EchoManager.getServerInstance().getEcho(echoId).ifPresentOrElse(
                                echo -> PacketDistributor.sendToPlayer(sp, new OpenEchoScreenMessage(echo.id())),
                                () -> sp.displayClientMessage(Component.literal("No such echo " + echoId), true)
                        );
                    } catch (ResourceLocationException e) {
                        FTBEchoes.LOGGER.error("bad resource location! {}:{}", namespace, path);
                    }
                }
            }
            event.setCanceled(true);
        }
    }

    private void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new EchoManager.ReloadListener(event.getRegistryAccess()));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
