package dev.ftb.mods.ftbechoes.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class FTBEchoesClient {
    public static void onModConstruction(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(FTBEchoesClient::clientSetup);
    }

    public static void clientSetup(FMLClientSetupEvent event) {
        // Client init
    }
}
