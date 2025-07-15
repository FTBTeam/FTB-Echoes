package dev.ftb.mods.ftbechoes.echo.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.echo.BaseStageEntry;
import dev.ftb.mods.ftbechoes.echo.StageEntryType;
import dev.ftb.mods.ftbechoes.registry.ModStageEntryTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record AudioEntry(ResourceLocation location) implements BaseStageEntry {
    public static String ID = "audio";

    public static final MapCodec<AudioEntry> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("location").forGetter(AudioEntry::location)
    ).apply(builder, AudioEntry::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AudioEntry> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AudioEntry::location,
            AudioEntry::new
    );

    @Override
    public StageEntryType<? extends BaseStageEntry> getType() {
        return ModStageEntryTypes.AUDIO_ENTRY.get();
    }
}
