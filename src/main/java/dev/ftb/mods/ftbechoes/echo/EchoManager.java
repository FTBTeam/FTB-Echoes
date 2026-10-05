package dev.ftb.mods.ftbechoes.echo;

import com.google.gson.JsonElement;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.ShopSummary;
import dev.ftb.mods.ftbechoes.net.SyncEchoesMessage;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import dev.ftb.mods.ftbechoes.shopping.ShopDataCache;
import dev.ftb.mods.ftbechoes.shopping.ShoppingKey;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EchoManager {
    @Nullable private static EchoManager clientInstance;
    @Nullable private static EchoManager serverInstance;

    private final Map<Identifier, Echo> echoes = new ConcurrentHashMap<>();
    private final ShopDataCache shoppingCache = new ShopDataCache(this);

    public static void initClient() {
        assert serverInstance == null;
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
        assert clientInstance == null;
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
        return Objects.requireNonNull(clientInstance);
    }

    public static EchoManager getServerInstance() {
        return Objects.requireNonNull(serverInstance);
    }

    public Collection<Echo> getEchoes() {
        return echoes.values();
    }

    public Optional<Echo> getEcho(@Nullable Identifier id) {
        return id == null ? Optional.empty() : Optional.ofNullable(echoes.get(id));
    }

    public boolean isKnownEcho(Identifier id) {
        return echoes.containsKey(id);
    }

    public void syncFromServer(Collection<Echo> echoes) {
        assert this == clientInstance;

        clear();
        echoes.forEach(echo -> this.echoes.put(echo.id(), echo));
        ShopSummary.INSTANCE.buildSummary();

        FTBEchoes.LOGGER.debug("{} echoes sync'd from server", echoes.size());
    }

    public void syncToClient(ServerPlayer sp) {
        PacketDistributor.sendToPlayer(sp, new SyncEchoesMessage(getEchoes()));
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

    public static class ReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
        public ReloadListener() {
            super(ExtraCodecs.JSON, FileToIdConverter.json("echo_definitions"));
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            EchoManager.initServer();

            getServerInstance().clear();

            map.forEach((id, json) -> Echo.fromJson(json, getRegistryLookup()).ifPresent(echo -> getServerInstance().echoes.put(id, echo)));

            FTBEchoes.LOGGER.info("loaded {} echo definitions", getServerInstance().echoes.size());

            if (ServerLifecycleHooks.getCurrentServer() != null) {
                PacketDistributor.sendToAllPlayers(new SyncEchoesMessage(EchoManager.getServerInstance().getEchoes()));
            }
        }
    }
}
