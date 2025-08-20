package dev.ftb.mods.ftbechoes.echo;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Optional;

public record Echo(ResourceLocation id, Component title, List<EchoStage> stages) {
    public static final Codec<Echo> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Echo::id),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(Echo::title),
            EchoStage.CODEC.listOf().fieldOf("stages").forGetter(Echo::stages)
    ).apply(builder, Echo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Echo> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, Echo::id,
            ComponentSerialization.STREAM_CODEC, Echo::title,
            EchoStage.STREAM_CODEC.apply(ByteBufCodecs.list()), Echo::stages,
            Echo::new
    );

    public static Optional<Echo> fromJson(JsonElement json, RegistryAccess registryAccess) {
        return CODEC.decode(registryAccess.createSerializationContext(JsonOps.INSTANCE), json)
                .resultOrPartial(error -> FTBEchoes.LOGGER.error("JSON parse failure: {}", error))
                .map(Pair::getFirst);
    }

    public Optional<ShopData> getShopData(String name) {
        for (EchoStage s : stages) {
            for (ShopData d : s.shopUnlocked()) {
                if (d.name().equals(name)) {
                    return Optional.of(d);
                }
            }
        }
        return Optional.empty();
    }
}
