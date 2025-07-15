package dev.ftb.mods.ftbechoes.registry;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.BaseStageEntry;
import dev.ftb.mods.ftbechoes.echo.StageEntryType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class RegistryKeys {
    // Keys
    public static final ResourceKey<Registry<StageEntryType<? extends BaseStageEntry>>> STAGE_ENTRY_KEY
            = ResourceKey.createRegistryKey(FTBEchoes.id("stage_entry"));

    // Registries
    public static final Registry<StageEntryType<? extends BaseStageEntry>> STAGE_ENTRY_REGISTRY
            = new RegistryBuilder<>(STAGE_ENTRY_KEY).sync(true).create();
}
