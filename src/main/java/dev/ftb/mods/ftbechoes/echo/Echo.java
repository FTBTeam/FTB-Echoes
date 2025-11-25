package dev.ftb.mods.ftbechoes.echo;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record Echo(ResourceLocation id, Component title, List<EchoStage> stages, Optional<Component> allComplete) {
    private static final Codec<Echo> RAW_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Echo::id),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(Echo::title),
            EchoStage.CODEC.listOf().fieldOf("stages").forGetter(Echo::stages),
            ComponentSerialization.CODEC.optionalFieldOf("all_complete").forGetter(Echo::allComplete)
    ).apply(builder, Echo::new));

    public static final Codec<Echo> CODEC = RAW_CODEC.validate(Echo::validate);

    public static final StreamCodec<RegistryFriendlyByteBuf, Echo> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, Echo::id,
            ComponentSerialization.STREAM_CODEC, Echo::title,
            EchoStage.STREAM_CODEC.apply(ByteBufCodecs.list()), Echo::stages,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), Echo::allComplete,
            Echo::new
    );

    public static Optional<Echo> fromJson(JsonElement json, RegistryAccess registryAccess) {
        return CODEC.decode(registryAccess.createSerializationContext(JsonOps.INSTANCE), json)
                .resultOrPartial(error -> FTBEchoes.LOGGER.error("JSON parse failure: {}", error))
                .map(Pair::getFirst);
    }

    private DataResult<Echo> validate() {
        Set<String> shopIds = new HashSet<>();
        for (var stage : stages) {
            for (var entry : stage.shopUnlocked()) {
                if (!shopIds.add(entry.name())) {
                    return DataResult.error(() -> String.format("duplicate shop key '%s' in echo id '%s'", entry.name(), id), this);
                }
            }
        }
        return DataResult.success(this);
    }

    public boolean hasAnyShopItems() {
        return stages.stream().anyMatch(s -> !s.shopUnlocked().isEmpty());
    }
}
