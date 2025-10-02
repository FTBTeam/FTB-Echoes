package dev.ftb.mods.ftbechoes.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PersistedClientData {
    private static final Codec<Map<ResourceLocation, Set<Integer>>> COLLAPSED_CODEC
            = Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT.listOf().xmap(HashSet::new, ArrayList::new))
            .xmap(HashMap::new, map -> {
                Map<ResourceLocation,HashSet<Integer>> res = new HashMap<>();
                map.forEach((k, v) -> res.put(k, new HashSet<>(v)));
                return res;
            });

    public static final Codec<PersistedClientData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            COLLAPSED_CODEC.fieldOf("collapsed").forGetter(p -> p.collapsedStages)
    ).apply(builder, PersistedClientData::new));

    private static final String DATA_FILE = "clientdata-{id}.snbt";

    private static PersistedClientData INSTANCE;
    private static UUID lastTeamId;

    private boolean saveNeeded = true;
    private final Map<ResourceLocation, Set<Integer>> collapsedStages;

    private PersistedClientData(Map<ResourceLocation, Set<Integer>> collapsedStages) {
        this.collapsedStages = collapsedStages;
    }

    private static PersistedClientData createNew() {
        return new PersistedClientData(new HashMap<>());
    }

    public static PersistedClientData get() {
        var teamId = FTBTeamsAPI.api().getClientManager().getManagerId();
        if (INSTANCE != null && !Objects.equals(lastTeamId, teamId)) {
            INSTANCE = null; // team changed, reload data
        }

        if (INSTANCE == null) {
            lastTeamId = teamId;
            Path file = savePath();
            if (!Files.exists(file)) {
                createNew().save();
            }
            try {
                SNBTCompoundTag tag = SNBT.tryRead(file);
                INSTANCE = CODEC.parse(NbtOps.INSTANCE, tag)
                        .resultOrPartial(FTBEchoes.LOGGER::error)
                        .orElse(createNew());
            } catch (IOException e) {
                FTBEchoes.LOGGER.error("can't read {}, using default persisted client data", file);
                INSTANCE = createNew();
            }
        }
        return INSTANCE;
    }

    public void save() {
        if (saveNeeded) {
            Path file = savePath();
            try {
                Tag tag = CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial(FTBEchoes.LOGGER::error).orElse(new CompoundTag());
                if (tag instanceof CompoundTag c) {
                    SNBT.tryWrite(file, c);
                } else {
                    FTBEchoes.LOGGER.error("can't write {}, expected CompoundTag?", file);
                }
            } catch (IOException e) {
                FTBEchoes.LOGGER.error("can't write {}", file);
            }
            saveNeeded = false;
        }
    }

    public boolean isStageCollapsed(Echo echo, int stageIdx) {
        return collapsedStages.getOrDefault(echo.id(), Set.of()).contains(stageIdx);
    }

    public boolean setStageCollapsed(Echo echo, int stageIdx, boolean collapsed) {
        boolean c = isStageCollapsed(echo, stageIdx);
        if (c != collapsed) {
            var set = collapsedStages.computeIfAbsent(echo.id(), k -> new HashSet<>());
            if (collapsed) {
                set.add(stageIdx);
            } else {
                set.remove(stageIdx);
            }
            saveNeeded = true;
            return true;
        }
        return false;
    }

    private static Path savePath() {
        var teamId = FTBTeamsAPI.api().getClientManager().getManagerId();
        return ConfigUtil.LOCAL_DIR.resolve("ftbechos").resolve(DATA_FILE.replace("{id}", teamId == null ? "default" : teamId.toString()));
    }
}
