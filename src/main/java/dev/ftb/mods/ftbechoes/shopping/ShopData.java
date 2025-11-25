package dev.ftb.mods.ftbechoes.shopping;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.echo.CommandInfo;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import dev.ftb.mods.ftbechoes.util.EchoCodecs;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ShopData(String name, List<ItemStack> stacks, int cost, List<Component> description, Optional<Icon> icon, Optional<CommandInfo> command, Optional<Integer> maxClaims, boolean perPlayerMax, int maxStage) {
    private static final Codec<ShopData> RAW_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(ShopData::name),
            EchoCodecs.ITEM_OR_ITEMS_CODEC.optionalFieldOf("item", List.of()).forGetter(ShopData::stacks),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("cost", 1).forGetter(ShopData::cost),
            EchoCodecs.COMPONENT_OR_LIST.optionalFieldOf("description", List.of()).forGetter(ShopData::description),
            Icon.STRING_CODEC.optionalFieldOf("icon").forGetter(ShopData::icon),
            CommandInfo.CODEC.optionalFieldOf("command").forGetter(ShopData::command),
            Codec.INT.optionalFieldOf("max_claims").forGetter(ShopData::maxClaims),
            Codec.BOOL.optionalFieldOf("per_player_max_claims", false).forGetter(ShopData::perPlayerMax),
            Codec.INT.optionalFieldOf("max_stage", Integer.MAX_VALUE).forGetter(ShopData::maxStage)
    ).apply(builder, ShopData::new));

    public static final Codec<ShopData> CODEC = RAW_CODEC.validate(ShopData::validate);

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopData> STREAM_CODEC = NetworkHelper.composite(
            ByteBufCodecs.STRING_UTF8, ShopData::name,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), ShopData::stacks,
            ByteBufCodecs.VAR_INT, ShopData::cost,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), ShopData::description,
            ByteBufCodecs.optional(Icon.STREAM_CODEC), ShopData::icon,
            ByteBufCodecs.optional(CommandInfo.STREAM_CODEC), ShopData::command,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), ShopData::maxClaims,
            ByteBufCodecs.BOOL, ShopData::perPlayerMax,
            ByteBufCodecs.VAR_INT, ShopData::maxStage,
            ShopData::new
    );

    private DataResult<ShopData> validate() {
        if (stacks.isEmpty() && (icon.isEmpty() || command.isEmpty())) {
            return DataResult.error(() -> "when item is empty, icon, description and command must all be specified");
        } else if (!stacks.isEmpty() && command.isPresent()) {
            return DataResult.error(() -> "when item is not empty, icon, command must not be specified");
        }
        return DataResult.success(this);
    }

    public void giveTo(ShoppingKey key, ServerPlayer player, int nOrders) {
        for (ItemStack stack : stacks()) {
            int total = stack.getCount() * nOrders;
            while (total > 0) {
                ItemStack toGive = stack.copyWithCount(Math.min(total, stack.getMaxStackSize()));
                ItemHandlerHelper.giveItemToPlayer(player, toGive);
                total -= toGive.getCount();
            }
        }
        command.ifPresent(cmdInfo -> cmdInfo.runForPlayer(player));
        if (maxClaims().isPresent()) {
            TeamProgressManager.get().consumeLimitedShopPurchase(player, key, nOrders, this);
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
