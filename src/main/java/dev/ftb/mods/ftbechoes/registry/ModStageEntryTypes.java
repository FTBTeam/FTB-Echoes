package dev.ftb.mods.ftbechoes.registry;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.BaseStageEntry;
import dev.ftb.mods.ftbechoes.echo.StageEntryType;
import dev.ftb.mods.ftbechoes.echo.entries.AudioEntry;
import dev.ftb.mods.ftbechoes.echo.entries.ImageEntry;
import dev.ftb.mods.ftbechoes.echo.entries.TextEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModStageEntryTypes {
    public static final DeferredRegister<StageEntryType<?>> STAGE_ENTRY_TYPES
            = DeferredRegister.create(RegistryKeys.STAGE_ENTRY_REGISTRY, FTBEchoes.MOD_ID);

    public static final Supplier<StageEntryType<TextEntry>> TEXT_ENTRY
            = registerStageEntryType(TextEntry.ID, TextEntry.CODEC, TextEntry.STREAM_CODEC);
    public static final Supplier<StageEntryType<ImageEntry>> IMAGE_ENTRY
            = registerStageEntryType(ImageEntry.ID, ImageEntry.CODEC, ImageEntry.STREAM_CODEC);
    public static final Supplier<StageEntryType<AudioEntry>> AUDIO_ENTRY
            = registerStageEntryType(AudioEntry.ID, AudioEntry.CODEC, AudioEntry.STREAM_CODEC);

    // ---------------

    private static <A extends BaseStageEntry, T extends StageEntryType<A>> Supplier<T> registerStageEntryType(
            String name, MapCodec<A> codec, StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec
    ) {
        //noinspection unchecked
        return STAGE_ENTRY_TYPES.register(name, () -> (T) new StageEntryType<>(codec, streamCodec));
    }
}
