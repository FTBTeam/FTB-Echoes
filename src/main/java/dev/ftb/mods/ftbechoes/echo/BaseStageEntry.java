package dev.ftb.mods.ftbechoes.echo;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbechoes.registry.RegistryKeys;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface BaseStageEntry {
    Codec<BaseStageEntry> CODEC = RegistryKeys.STAGE_ENTRY_REGISTRY.byNameCodec().dispatch(
            BaseStageEntry::getType,
            StageEntryType::codec
    );
    StreamCodec<RegistryFriendlyByteBuf, BaseStageEntry> STREAM_CODEC
            = ByteBufCodecs.registry(RegistryKeys.STAGE_ENTRY_KEY)
            .dispatch(BaseStageEntry::getType, StageEntryType::streamCodec);

    StageEntryType<? extends BaseStageEntry> getType();

    String getTypeId();
}
