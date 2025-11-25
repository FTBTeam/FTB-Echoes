package dev.ftb.mods.ftbechoes;

import dev.ftb.mods.ftbechoes.command.FTBEchoesCommands;
import dev.ftb.mods.ftbechoes.command.NBTEditCommand;
import dev.ftb.mods.ftbechoes.config.FTBEchoesServerConfig;
import dev.ftb.mods.ftbechoes.datagen.DataGenerators;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import dev.ftb.mods.ftbechoes.net.SyncProgressMessage;
import dev.ftb.mods.ftbechoes.registry.*;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.integration.currency.CurrencyHelper;
import dev.ftb.mods.ftblibrary.integration.currency.CurrencyProvider;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftblibrary.integration.stages.StageProvider;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditResponseHandlers;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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

    public static final Lazy<CurrencyProvider> CURRENCY_PROVIDER
            = Lazy.of(() -> CurrencyHelper.getInstance().getProvider());
    public static final Lazy<StageProvider> STAGE_PROVIDER
            = Lazy.of(() -> StageHelper.getInstance().getProvider());

    public FTBEchoes(IEventBus eventBus) {
        IEventBus forgeBus = NeoForge.EVENT_BUS;

        ConfigManager.getInstance().registerServerConfig(FTBEchoesServerConfig.CONFIG, MOD_ID + ".server_config", false);

        eventBus.addListener(this::addCreative);
        eventBus.addListener(DataGenerators::gatherData);
        eventBus.addListener(this::onNewRegistry);

        registerAll(eventBus);

        forgeBus.addListener(this::onServerAboutToStart);
        forgeBus.addListener(this::onServerStopped);
        forgeBus.addListener(this::onPlayerLogin);
        forgeBus.addListener(this::registerReloadListeners);
        forgeBus.addListener(FTBEchoesCommands::registerCommands);

        TeamEvent.PLAYER_LOGGED_IN.register(this::onPlayerTeamLogin);
        TeamEvent.PLAYER_CHANGED.register(this::onPlayerTeamChange);
    }

    public static CurrencyProvider currencyProvider() {
        return CURRENCY_PROVIDER.get();
    }

    public static StageProvider stageProvider() {
        return STAGE_PROVIDER.get();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == FTBLibrary.getCreativeModeTab().get()) {
            event.accept(ModBlocks.ECHO_PROJECTOR.get());
        }
    }

    private static void registerAll(IEventBus eventBus) {
        ModBlocks.BLOCKS.register(eventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(eventBus);
        ModEntityTypes.ENTITY_TYPES.register(eventBus);
        ModItems.ITEMS.register(eventBus);
        ModStageEntryTypes.STAGE_ENTRY_TYPES.register(eventBus);
        ModArgumentTypes.COMMAND_ARGUMENT_TYPES.register(eventBus);
        ModSounds.SOUNDS.register(eventBus);
    }

    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        EchoManager.initServer();

        NBTEditResponseHandlers.INSTANCE.registerHandler(NBTEditCommand.FTBECHOES_PROGRESS, (serverPlayer, info, data) -> {
            FTBTeamsAPI.api().getManager().getTeamForPlayerID(info.getUUID("id")).ifPresent(team -> {
                TeamProgress.CODEC.parse(NbtOps.INSTANCE, data)
                        .ifSuccess(progress -> {
                            serverPlayer.displayClientMessage(Component.translatable("ftbechoes.message.progress_edited", team.getColoredName()), false);
                            TeamProgressManager.get(event.getServer()).injectProgressData(team.getTeamId(), progress);
                        });
            });
        });
    }

    private void onServerStopped(ServerStoppedEvent event) {
        EchoManager.shutdownServer();
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            EchoManager.getServerInstance().syncToClient(sp);
            checkForAutoclaimRewards(sp);
        }
    }

    private void checkForAutoclaimRewards(ServerPlayer sp) {
        TeamProgressManager mgr = TeamProgressManager.get(sp.getServer());
        mgr.getProgress(sp).ifPresent(progress -> {
            var res = progress.checkForAutoclaim(sp);
            if (!res.isEmpty()) {
                PacketDistributor.sendToPlayer(sp, SyncProgressMessage.forPlayer(progress, sp));
                sp.displayClientMessage(Component.translatable("ftbechoes.message.reward_claimed_offline"), false);
                res.forEach(echoAndStage -> {
                    Component msg = Component.literal("• ")
                            .append(echoAndStage.getFirst().title())
                            .append(" | ")
                            .append(echoAndStage.getFirst().stages().get(echoAndStage.getSecond()).title());
                    sp.displayClientMessage(msg, false);
                });
                mgr.setDirty();
            }
        });
    }

    private void onPlayerTeamLogin(PlayerLoggedInAfterTeamEvent event) {
        var player = event.getPlayer();
        var server = Objects.requireNonNull(player.getServer());
        var progress = TeamProgressManager.get(server).getProgress(event.getTeam());
        PacketDistributor.sendToPlayer(player, SyncProgressMessage.forPlayer(progress, player));

        if (FTBEchoesServerConfig.SYNC_STAGES.get()) {
            StageHelper.getInstance().getProvider().sync(player);
        }
    }

    private void onPlayerTeamChange(PlayerChangedTeamEvent event) {
        if (event.getPlayer() instanceof ServerPlayer sp) {
            var server = Objects.requireNonNull(sp.getServer());
            var progress = TeamProgressManager.get(server).getProgress(event.getTeam());
            PacketDistributor.sendToPlayer(sp, SyncProgressMessage.forPlayer(progress, sp));
        }
    }

    private void onNewRegistry(NewRegistryEvent event) {
        event.register(RegistryKeys.STAGE_ENTRY_REGISTRY);
    }

    private void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new EchoManager.ReloadListener(event.getRegistryAccess()));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
