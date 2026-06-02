package dev.ftb.mods.ftbechoes.echo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public record EchoModel(boolean show, VillagerData data, boolean showHat) {
    public static final Codec<EchoModel> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.BOOL.optionalFieldOf("show", true).forGetter(EchoModel::show),
            VillagerData.CODEC.fieldOf("villager_data").forGetter(EchoModel::data),
            Codec.BOOL.optionalFieldOf("show_hat", true).forGetter(EchoModel::showHat)
    ).apply(builder, EchoModel::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EchoModel> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EchoModel::show,
            VillagerData.STREAM_CODEC, EchoModel::data,
            ByteBufCodecs.BOOL, EchoModel::showHat,
            EchoModel::new
    );

    public static final EchoModel NONE = new EchoModel(
            false,
            new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1),
            false
    );
}
