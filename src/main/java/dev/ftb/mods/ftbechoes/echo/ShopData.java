package dev.ftb.mods.ftbechoes.echo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public record ShopData(ItemStack stack, int cost) {
    public static final Codec<ShopData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemStack.CODEC.fieldOf("item").forGetter(ShopData::stack),
            ExtraCodecs.POSITIVE_INT.fieldOf("cost").forGetter(ShopData::cost)
    ).apply(builder, ShopData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ShopData::stack,
            ByteBufCodecs.VAR_INT, ShopData::cost,
            ShopData::new
    );
}
