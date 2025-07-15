package dev.ftb.mods.ftbechoes.echo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.architectury.utils.GameInstance;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.net.SyncEchoesMessage;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EchoManager {
    private static final EchoManager CLIENT_INSTANCE = new EchoManager();
    private static final EchoManager SERVER_INSTANCE = new EchoManager();

    private final Map<ResourceLocation, Echo> templates = new ConcurrentHashMap<>();

    public static EchoManager getClientInstance() {
        return CLIENT_INSTANCE;
    }

    public static EchoManager getServerInstance() {
        return SERVER_INSTANCE;
    }

    public Collection<Echo> getEchoes() {
        return templates.values();
    }

    public Optional<Echo> getEcho(ResourceLocation id) {
        return Optional.ofNullable(templates.get(id));
    }

    public void syncFromServer(Collection<Echo> echoes) {
        assert this == CLIENT_INSTANCE;

        templates.clear();
        echoes.forEach(echo -> templates.put(echo.id(), echo));
    }

    public void syncToClient(ServerPlayer sp) {
        PacketDistributor.sendToPlayer(sp, new SyncEchoesMessage(getEchoes()));
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

        public ReloadListener() {
            super(GSON, "echo_definitions");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            Map<ResourceLocation, Echo> serverTemplates = getServerInstance().templates;

            serverTemplates.clear();

            map.forEach((id, json) -> Echo.fromJson(json).ifPresent(echo -> serverTemplates.put(id, echo)));

            FTBEchoes.LOGGER.info("loaded {} echo definitions", serverTemplates.size());

            if (ServerLifecycleHooks.getCurrentServer() != null) {
                PacketDistributor.sendToAllPlayers(new SyncEchoesMessage(EchoManager.getServerInstance().getEchoes()));
            }
        }
    }
}
