package dev.ftb.mods.ftbechoes.shopping;

import dev.ftb.mods.ftbechoes.echo.Echo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Uniquely identifies one shopping entry in an Echo
 *
 * @param echoId the echo's unique ID
 * @param name the shop data ID, which must be unique within its Echo
 */
public record ShoppingKey(ResourceLocation echoId, String name) {
    public static final StreamCodec<FriendlyByteBuf, ShoppingKey> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ShoppingKey::echoId,
            ByteBufCodecs.STRING_UTF8, ShoppingKey::name,
            ShoppingKey::new
    );

    public static ShoppingKey of(Echo echo, ShopData data) {
        return new ShoppingKey(echo.id(), data.name());
    }
}
