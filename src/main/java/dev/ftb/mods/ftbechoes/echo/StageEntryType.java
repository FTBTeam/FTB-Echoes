package dev.ftb.mods.ftbechoes.echo;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record StageEntryType<S extends BaseStageEntry>(
        MapCodec<S> codec,
        StreamCodec<? super RegistryFriendlyByteBuf, S> streamCodec
) { }
