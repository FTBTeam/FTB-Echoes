package dev.ftb.mods.ftbechoes.echo.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.echo.BaseStageEntry;
import dev.ftb.mods.ftbechoes.echo.StageEntryType;
import dev.ftb.mods.ftbechoes.registry.ModStageEntryTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record ImageEntry(ResourceLocation location, int width, int height, Alignment alignment) implements BaseStageEntry {
    public static String ID = "image";

    public static final MapCodec<ImageEntry> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("location").forGetter(ImageEntry::location),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("width", 32).forGetter(ImageEntry::width),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("height", 32).forGetter(ImageEntry::height),
            StringRepresentable.fromEnum(Alignment::values).optionalFieldOf("align", Alignment.LEFT).forGetter(ImageEntry::alignment)
    ).apply(builder, ImageEntry::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ImageEntry> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ImageEntry::location,
            ByteBufCodecs.VAR_INT, ImageEntry::width,
            ByteBufCodecs.VAR_INT, ImageEntry::height,
            NeoForgeStreamCodecs.enumCodec(Alignment.class), ImageEntry::alignment,
            ImageEntry::new
    );

    @Override
    public StageEntryType<? extends BaseStageEntry> getType() {
        return ModStageEntryTypes.IMAGE_ENTRY.get();
    }

    @Override
    public String getTypeId() {
        return ID;
    }

    public enum Alignment implements StringRepresentable {
        LEFT("left"),
        CENTER("center"),
        RIGHT("right");

        private final String name;

        Alignment(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
