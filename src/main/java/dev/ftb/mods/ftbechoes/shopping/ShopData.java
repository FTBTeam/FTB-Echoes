package dev.ftb.mods.ftbechoes.shopping;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Objects;
import java.util.Optional;

public record ShopData(String name, ItemStack stack, int cost, Optional<Component> description, Optional<Icon> icon, String command, int permissionLevel) {
    // TODO move into FTB Library
    public static final Codec<Icon> ICON_STRING_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                Icon res = Icon.getIcon(s);
                return res.isEmpty() ? DataResult.error(() -> "Invalid icon spec: " + s) : DataResult.success(res);
            },
            Icon::toString
    );

    private static final Codec<ShopData> RAW_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(ShopData::name),
            ItemStack.CODEC.optionalFieldOf("item", ItemStack.EMPTY).forGetter(ShopData::stack),
            ExtraCodecs.POSITIVE_INT.fieldOf("cost").forGetter(ShopData::cost),
            ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(ShopData::description),
            ICON_STRING_CODEC.optionalFieldOf("icon").forGetter(ShopData::icon),
            Codec.STRING.optionalFieldOf("command", "").forGetter(ShopData::command),
            ExtraCodecs.intRange(1, 4).optionalFieldOf("permission_level", 1).forGetter(ShopData::permissionLevel)
    ).apply(builder, ShopData::new));

    public static final Codec<ShopData> CODEC = RAW_CODEC.validate(ShopData::validate);

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopData> STREAM_CODEC = NetworkHelper.composite(
            ByteBufCodecs.STRING_UTF8, ShopData::name,
            ItemStack.OPTIONAL_STREAM_CODEC, ShopData::stack,
            ByteBufCodecs.VAR_INT, ShopData::cost,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, ShopData::description,
            ByteBufCodecs.optional(Icon.STREAM_CODEC), ShopData::icon,
            ByteBufCodecs.STRING_UTF8, ShopData::command,
            ByteBufCodecs.VAR_INT, ShopData::permissionLevel,
            ShopData::new
    );

    private DataResult<ShopData> validate() {
        if (stack.isEmpty() && (icon.isEmpty() || command.isEmpty() || description.isEmpty())) {
            return DataResult.error(() -> "when item is empty, icon, description and command must all be specified");
        } else if (!stack.isEmpty() && !command.isEmpty()) {
            return DataResult.error(() -> "when item is not empty, icon, command must not be specified");
        }
        return DataResult.success(this);
    }

    public void giveTo(ServerPlayer player, int nOrders) {
        if (!stack().isEmpty()) {
            int total = stack().getCount() * nOrders;
            while (total > 0) {
                ItemStack toGive = stack().copyWithCount(Math.min(total, stack().getMaxStackSize()));
                ItemHandlerHelper.giveItemToPlayer(player, toGive);
                total -= toGive.getCount();
            }
        }
        if (!command().isEmpty()) {
            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(2), command());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopData data = (ShopData) o;
        return Objects.equals(name, data.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
