package dev.ftb.mods.ftbechoes.echo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.util.EchoCodecs;
import dev.ftb.mods.ftbechoes.util.MiscUtil;
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

public record StageCompletionReward(List<ItemStack> stacks, int exp, int currency, Optional<CommandInfo> command, List<Component> description) {
    public static final Codec<StageCompletionReward> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            EchoCodecs.ITEM_OR_ITEMS_CODEC.optionalFieldOf("item", List.of()).forGetter(StageCompletionReward::stacks),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("experience", 0).forGetter(StageCompletionReward::exp),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("currency", 0).forGetter(StageCompletionReward::currency),
            CommandInfo.CODEC.optionalFieldOf("command").forGetter(StageCompletionReward::command),
            EchoCodecs.COMPONENT_OR_LIST.optionalFieldOf("description", List.of()).forGetter(StageCompletionReward::description)
    ).apply(builder, StageCompletionReward::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StageCompletionReward> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), StageCompletionReward::stacks,
            ByteBufCodecs.VAR_INT, StageCompletionReward::exp,
            ByteBufCodecs.VAR_INT, StageCompletionReward::currency,
            ByteBufCodecs.optional(CommandInfo.STREAM_CODEC), StageCompletionReward::command,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), StageCompletionReward::description,
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
        command.ifPresent(cmdInfo -> cmdInfo.runForPlayer(player));
    }

    public void addTooltip(Consumer<Component> consumer) {
        description.forEach(consumer);

        for (ItemStack stack : stacks) {
            consumer.accept(bullet().append(stack.getCount() + " x ").append(stack.getHoverName()));
        }
        if (exp > 0) {
            consumer.accept(bullet().append(exp + " XP"));
        }
        if (currency > 0) {
            consumer.accept(bullet().append(MiscUtil.formatCost(currency)));
        }
    }

    private static MutableComponent bullet() {
        return Component.literal("• ").withStyle(ChatFormatting.AQUA);
    }
}
