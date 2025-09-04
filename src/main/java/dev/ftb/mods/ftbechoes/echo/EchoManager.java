package dev.ftb.mods.ftbechoes.echo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.net.SyncEchoesMessage;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import dev.ftb.mods.ftbechoes.shopping.ShopDataCache;
import dev.ftb.mods.ftbechoes.shopping.ShoppingKey;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EchoManager {
    private static EchoManager clientInstance;
    private static EchoManager serverInstance;

    private final Map<ResourceLocation, Echo> echoes = new ConcurrentHashMap<>();
    private final ShopDataCache shoppingCache = new ShopDataCache();

    public static void initClient() {
        if (clientInstance == null) {
            clientInstance = new EchoManager();
        }
    }

    public static void shutdownClient() {
        if (clientInstance != null) {
            clientInstance.clear();
        }
        clientInstance = null;
    }

    public static void initServer() {
        if (serverInstance == null) {
            serverInstance = new EchoManager();
        }
    }

    public static void shutdownServer() {
        if (serverInstance != null) {
            serverInstance.clear();
        }
        serverInstance = null;
    }

    public static EchoManager getInstance() {
        return Objects.requireNonNullElse(clientInstance, serverInstance);
    }

    public static EchoManager getClientInstance() {
        return clientInstance;
    }

    public static EchoManager getServerInstance() {
        return serverInstance;
    }

    public Collection<Echo> getEchoes() {
        return echoes.values();
    }

    public Optional<Echo> getEcho(ResourceLocation id) {
        return Optional.ofNullable(echoes.get(id));
    }

    public boolean isKnownEcho(ResourceLocation id) {
        return echoes.containsKey(id);
    }

    public void syncFromServer(Collection<Echo> echoes) {
        assert this == clientInstance;

        clear();
        echoes.forEach(echo -> this.echoes.put(echo.id(), echo));
    }

    public void syncToClient(ServerPlayer sp) {
        PacketDistributor.sendToPlayer(sp, new SyncEchoesMessage(getEchoes()));
    }

    public void validateEchoId(ResourceLocation echoId) {
        Validate.isTrue(echoes.containsKey(echoId), "Unknown echo ID: " + echoId);
    }

    public Optional<ShopDataCache.ShoppingEntry> getShoppingEntry(ShoppingKey key) {
        return Optional.ofNullable(shoppingCache.find(key));
    }

    public Optional<ShopData> getShopData(ShoppingKey key) {
        return getShoppingEntry(key).map(ShopDataCache.ShoppingEntry::data);
    }

    public void clear() {
        echoes.clear();
        shoppingCache.clear();
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        private final RegistryAccess registryAccess;

        public ReloadListener(RegistryAccess registryAccess) {
            super(GSON, "echo_definitions");

            this.registryAccess = registryAccess;
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            EchoManager.initServer();

            getServerInstance().clear();

            map.forEach((id, json) -> Echo.fromJson(json, registryAccess).ifPresent(echo -> getServerInstance().echoes.put(id, echo)));

            FTBEchoes.LOGGER.info("loaded {} echo definitions", getServerInstance().echoes.size());

            if (ServerLifecycleHooks.getCurrentServer() != null) {
                PacketDistributor.sendToAllPlayers(new SyncEchoesMessage(EchoManager.getServerInstance().getEchoes()));
            }
        }
    }
}
