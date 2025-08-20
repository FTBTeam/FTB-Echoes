package dev.ftb.mods.ftbechoes.echo.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.echo.BaseStageEntry;
import dev.ftb.mods.ftbechoes.echo.StageEntryType;
import dev.ftb.mods.ftbechoes.registry.ModStageEntryTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record AudioEntry(ResourceLocation location, Optional<Component> text) implements BaseStageEntry {
    public static String ID = "audio";

    public static final MapCodec<AudioEntry> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("location").forGetter(AudioEntry::location),
            ComponentSerialization.CODEC.optionalFieldOf("text").forGetter(AudioEntry::text)
    ).apply(builder, AudioEntry::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AudioEntry> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AudioEntry::location,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, AudioEntry::text,
            AudioEntry::new
    );

    @Override
    public StageEntryType<? extends BaseStageEntry> getType() {
        return ModStageEntryTypes.AUDIO_ENTRY.get();
    }

    @Override
    public String getTypeId() {
        return ID;
    }
}
