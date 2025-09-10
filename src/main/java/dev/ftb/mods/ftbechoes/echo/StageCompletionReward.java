package dev.ftb.mods.ftbechoes.echo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.MiscUtil;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public record StageCompletionReward(List<ItemStack> stacks, int exp, int currency, String command, int permissionLevel, Optional<Component> commandDesc) {
    public static final Codec<StageCompletionReward> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ShopData.ITEM_OR_ITEMS_CODEC.optionalFieldOf("item", List.of()).forGetter(StageCompletionReward::stacks),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("experience", 0).forGetter(StageCompletionReward::exp),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("currency", 0).forGetter(StageCompletionReward::currency),
            Codec.STRING.optionalFieldOf("command", "").forGetter(StageCompletionReward::command),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("command_perm", 0).forGetter(StageCompletionReward::permissionLevel),
            ComponentSerialization.CODEC.optionalFieldOf("command_desc").forGetter(StageCompletionReward::commandDesc)
    ).apply(builder, StageCompletionReward::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StageCompletionReward> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), StageCompletionReward::stacks,
            ByteBufCodecs.VAR_INT, StageCompletionReward::exp,
            ByteBufCodecs.VAR_INT, StageCompletionReward::currency,
            ByteBufCodecs.STRING_UTF8, StageCompletionReward::command,
            ByteBufCodecs.VAR_INT, StageCompletionReward::permissionLevel,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), StageCompletionReward::commandDesc,
            StageCompletionReward::new
    );

    public void giveToPlayer(ServerPlayer player) {
        stacks.forEach(stack -> ItemHandlerHelper.giveItemToPlayer(player, stack.copy()));
        if (exp > 0) {
            player.giveExperiencePoints(exp);
        }
        if (currency > 0) {
            FTBEchoes.currencyProvider().giveCurrency(player, currency);
        }
        if (!command.isEmpty() && player.getServer() != null) {
            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(permissionLevel), command);
        }
    }

    public void addTooltip(Consumer<Component> consumer) {
        for (ItemStack stack : stacks) {
            consumer.accept(bullet().append(stack.getHoverName()));
        }
        if (exp > 0) {
            consumer.accept(bullet().append(exp + " XP"));
        }
        if (currency > 0) {
            consumer.accept(bullet().append(MiscUtil.formatCost(currency)));
        }
        if (!command.isEmpty()) {
            commandDesc.ifPresent(c -> consumer.accept(bullet().append(c)));
        }
    }

    private static MutableComponent bullet() {
        return Component.literal("• ").withStyle(ChatFormatting.AQUA);
    }
}
