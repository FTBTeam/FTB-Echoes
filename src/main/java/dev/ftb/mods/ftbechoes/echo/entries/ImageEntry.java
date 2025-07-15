package dev.ftb.mods.ftbechoes.echo.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.echo.BaseStageEntry;
import dev.ftb.mods.ftbechoes.echo.StageEntryType;
import dev.ftb.mods.ftbechoes.registry.ModStageEntryTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ImageEntry(ResourceLocation location) implements BaseStageEntry {
    public static String ID = "image";

    public static final MapCodec<ImageEntry> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("location").forGetter(ImageEntry::location)
    ).apply(builder, ImageEntry::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ImageEntry> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ImageEntry::location,
            ImageEntry::new
    );

    @Override
    public StageEntryType<? extends BaseStageEntry> getType() {
        return ModStageEntryTypes.IMAGE_ENTRY.get();
    }
}
