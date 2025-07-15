package dev.ftb.mods.ftbechoes.config;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;

public interface FTBEchoesServerConfig {
    String KEY = FTBEchoes.MOD_ID + "-server";

    SNBTConfig CONFIG = SNBTConfig.create(KEY)
            .comment("Server-specific configuration for FTB Echoes",
                    "Modpack defaults should be defined in <instance>/config/" + KEY + ".snbt",
                    "  (may be overwritten on modpack update)",
                    "Server admins may locally override this by copying into <instance>/world/serverconfig/" + KEY + ".snbt",
                    "  (will NOT be overwritten on modpack update)"
            );

    SNBTConfig GENERAL = CONFIG.addGroup("general");

}
