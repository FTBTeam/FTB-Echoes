package dev.ftb.mods.ftbechoes.config;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
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

    BooleanValue SYNC_STAGES = GENERAL.addBoolean("sync_stage_tags", true)
            .comment("If true, player entity tags (used for game stages) are sync'd to the client on login.",
                    "If another mod is handling this (e.g. KubeJS), you can set this to false to save a little network traffic",
                    "But leave as true otherwise, or Echo progression will break!");
}
