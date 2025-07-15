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

public record TextEntry(Component text) implements BaseStageEntry {
    public static String ID = "text";

    public static final MapCodec<TextEntry> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(TextEntry::text)
    ).apply(builder, TextEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TextEntry> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, TextEntry::text,
            TextEntry::new
    );

    @Override
    public StageEntryType<? extends BaseStageEntry> getType() {
        return ModStageEntryTypes.TEXT_ENTRY.get();
    }
}
