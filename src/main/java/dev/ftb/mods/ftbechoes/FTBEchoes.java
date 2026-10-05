package dev.ftb.mods.ftbechoes;

import dev.ftb.mods.ftbechoes.command.FTBEchoesCommands;
import dev.ftb.mods.ftbechoes.command.NBTEditCommand;
import dev.ftb.mods.ftbechoes.datagen.DataGenerators;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import dev.ftb.mods.ftbechoes.entity.EchoEntity;
import dev.ftb.mods.ftbechoes.net.SyncProgressMessage;
import dev.ftb.mods.ftbechoes.registry.*;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.integration.currency.CurrencyHelper;
import dev.ftb.mods.ftblibrary.integration.currency.CurrencyProvider;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftblibrary.integration.stages.StageProvider;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditResponseHandlers;
import dev.ftb.mods.ftbteams.api.neoforge.FTBTeamsEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
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

        eventBus.addListener(this::addCreative);
        eventBus.addListener(DataGenerators::gatherData);
        eventBus.addListener(this::onNewRegistry);
        eventBus.addListener(this::registerEntityAttributes);

        registerAll(eventBus);

        forgeBus.addListener(this::onServerAboutToStart);
        forgeBus.addListener(this::onServerStopped);
        forgeBus.addListener(this::onPlayerLogin);
        forgeBus.addListener(this::registerReloadListeners);
        forgeBus.addListener(FTBEchoesCommands::registerCommands);

        forgeBus.addListener(this::onPlayerTeamLogin);
        forgeBus.addListener(this::onPlayerTeamChange);
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

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.ECHO.get(), EchoEntity.createAttributes().build());
    }

    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        EchoManager.initServer();

        NBTEditResponseHandlers.INSTANCE.registerHandler(NBTEditCommand.FTBECHOES_PROGRESS, NBTEditCommand::handleResponse);
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
        TeamProgressManager mgr = TeamProgressManager.get(sp.level().getServer());
        mgr.getProgress(sp).ifPresent(progress -> {
            var res = progress.checkForAutoclaim(sp);
            if (!res.isEmpty()) {
                PacketDistributor.sendToPlayer(sp, SyncProgressMessage.forPlayer(progress, sp));
                sp.sendSystemMessage(Component.translatable("ftbechoes.message.reward_claimed_offline"));
                res.forEach(echoAndStage -> {
                    Component msg = Component.literal("• ")
                            .append(echoAndStage.getFirst().title())
                            .append(" | ")
                            .append(echoAndStage.getFirst().stages().get(echoAndStage.getSecond()).title());
                    sp.sendSystemMessage(msg);
                });
                mgr.setDirty();
            }
        });
    }

    private void onPlayerTeamLogin(FTBTeamsEvent.TeamPlayerLoggedIn event) {
        var data = event.getEventData();
        var player = data.player();
        var server = Objects.requireNonNull(player.level().getServer());
        var team = data.team();
        LOGGER.debug("Player {} login: team='{}' teamId={} getId={} teamType={}",
                player.getGameProfile().name(), team.getShortName(), team.getTeamId(), team.getId(), team.getClass().getSimpleName());
        var progress = TeamProgressManager.get(server).getProgress(team);
        LOGGER.debug("Progress for team {}: {} echoes tracked, stages={}",
                team.getTeamId(),
                progress.perEcho().size(),
                progress.perEcho().entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue().getCurrentStage())
                        .collect(java.util.stream.Collectors.joining(", ")));
        PacketDistributor.sendToPlayer(player, SyncProgressMessage.forPlayer(progress, player));
    }

    private void onPlayerTeamChange(FTBTeamsEvent.PlayerChangedTeam event) {
        var data = event.getEventData();
        if (data.player() instanceof ServerPlayer sp) {
            var server = Objects.requireNonNull(sp.level().getServer());
            var progress = TeamProgressManager.get(server).getProgress(data.team());
            PacketDistributor.sendToPlayer(sp, SyncProgressMessage.forPlayer(progress, sp));
        }
    }

    private void onNewRegistry(NewRegistryEvent event) {
        event.register(RegistryKeys.STAGE_ENTRY_REGISTRY);
    }

    private void registerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(FTBEchoes.id("echoes"), new EchoManager.ReloadListener());
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
