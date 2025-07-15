package dev.ftb.mods.ftbechoes.echo;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record Echo(ResourceLocation id, Component title, List<Stage> stages) {
    public static final Codec<Echo> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Echo::id),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(Echo::title),
            Stage.CODEC.listOf().fieldOf("stages").forGetter(Echo::stages)
    ).apply(builder, Echo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Echo> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, Echo::id,
            ComponentSerialization.STREAM_CODEC, Echo::title,
            Stage.STREAM_CODEC.apply(ByteBufCodecs.list()), Echo::stages,
            Echo::new
    );

    public static Optional<Echo> fromJson(JsonElement json) {
        return CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> FTBEchoes.LOGGER.error("JSON parse failure: {}", error))
                .map(Pair::getFirst);
    }

    public record Stage(List<BaseStageEntry> lore, List<BaseStageEntry> readyToProgress, String requiredStage, List<ShopData> shopUnlocked) {
        public static final Codec<Stage> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BaseStageEntry.CODEC.listOf().fieldOf("lore").forGetter(Stage::lore),
                BaseStageEntry.CODEC.listOf().fieldOf("ready_to_progress").forGetter(Stage::readyToProgress),
                Codec.STRING.fieldOf("required_stage").forGetter(Stage::requiredStage),
                ShopData.CODEC.listOf().fieldOf("shop_unlock").forGetter(Stage::shopUnlocked)
        ).apply(builder, Stage::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Stage> STREAM_CODEC = StreamCodec.composite(
                BaseStageEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), Stage::lore,
                BaseStageEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), Stage::readyToProgress,
                ByteBufCodecs.STRING_UTF8, Stage::requiredStage,
                ShopData.STREAM_CODEC.apply(ByteBufCodecs.list()), Stage::shopUnlocked,
                Stage::new
        );
    }
}
