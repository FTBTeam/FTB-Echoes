package dev.ftb.mods.ftbechoes.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.marhali.json5.Json5Object;
import de.marhali.json5.exception.Json5Exception;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftblibrary.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PersistedClientData {
    private static final Codec<Map<Identifier, Set<Integer>>> COLLAPSED_CODEC
            = Codec.unboundedMap(Identifier.CODEC, Codec.INT.listOf().xmap(HashSet::new, ArrayList::new))
            .xmap(HashMap::new, map -> {
                Map<Identifier,HashSet<Integer>> res = new HashMap<>();
                map.forEach((k, v) -> res.put(k, new HashSet<>(v)));
                return res;
            });

    public static final Codec<PersistedClientData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            COLLAPSED_CODEC.fieldOf("collapsed").forGetter(p -> p.collapsedStages)
    ).apply(builder, PersistedClientData::new));

    private static final String DATA_FILE = "clientdata-{id}.snbt";

    @Nullable
    private static PersistedClientData INSTANCE;

    private boolean saveNeeded = true;
    private final Map<Identifier, Set<Integer>> collapsedStages;

    private PersistedClientData(Map<Identifier, Set<Integer>> collapsedStages) {
        this.collapsedStages = collapsedStages;
    }

    private static PersistedClientData createNew() {
        return new PersistedClientData(new HashMap<>());
    }

    public static PersistedClientData get() {
        if (INSTANCE == null) {
            Path file = savePath();
            if (!Files.exists(file)) {
                createNew().save();
            }
            try {
                Json5Object tag = Json5Util.load(file);
                INSTANCE = CODEC.parse(Json5Ops.INSTANCE, tag)
                        .resultOrPartial(FTBEchoes.LOGGER::error)
                        .orElse(createNew());
            } catch (IOException | Json5Exception e) {
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
                var el = CODEC.encodeStart(Json5Ops.INSTANCE, this).resultOrPartial(FTBEchoes.LOGGER::error).orElse(new Json5Object());
                if (el instanceof Json5Object json) {
                    Json5Util.save(file, json);
                } else {
                    FTBEchoes.LOGGER.error("can't write {}, expected Json5Object?", file);
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
            var set = collapsedStages.computeIfAbsent(echo.id(), _ -> new HashSet<>());
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

    public static void refreshInstance() {
        INSTANCE = null;
    }

    private static Path savePath() {
        var teamId = FTBTeamsAPI.api().getClientManager().getManagerId();
        return ConfigUtil.LOCAL_DIR.resolve(FTBEchoes.MOD_ID).resolve(DATA_FILE.replace("{id}", teamId.toString()));
    }
}
