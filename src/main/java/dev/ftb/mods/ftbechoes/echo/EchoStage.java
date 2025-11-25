package dev.ftb.mods.ftbechoes.echo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;

public record EchoStage(Component title, boolean showTitleInLore, List<BaseStageEntry> lore,
                        Component notReady, Component ready, Optional<Component> completed,
                        String requiredGameStage, List<ShopData> shopUnlocked,
                        Optional<StageCompletionReward> completionReward)
{
    public static final Codec<EchoStage> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("title").forGetter(EchoStage::title),
            Codec.BOOL.optionalFieldOf("show_title_in_lore", true).forGetter(EchoStage::showTitleInLore),
            BaseStageEntry.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("lore").forGetter(EchoStage::lore),
            ComponentSerialization.CODEC.fieldOf("not_ready").forGetter(EchoStage::notReady),
            ComponentSerialization.CODEC.fieldOf("ready").forGetter(EchoStage::ready),
            ComponentSerialization.CODEC.optionalFieldOf("completed").forGetter(EchoStage::completed),
            Codec.STRING.fieldOf("required_stage").forGetter(EchoStage::requiredGameStage),
            ShopData.CODEC.listOf().optionalFieldOf("shop_unlock", List.of()).forGetter(EchoStage::shopUnlocked),
            StageCompletionReward.CODEC.optionalFieldOf("completion_reward").forGetter(EchoStage::completionReward)
    ).apply(builder, EchoStage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EchoStage> STREAM_CODEC = NetworkHelper.composite(
            ComponentSerialization.STREAM_CODEC, EchoStage::title,
            ByteBufCodecs.BOOL, EchoStage::showTitleInLore,
            BaseStageEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), EchoStage::lore,
            ComponentSerialization.STREAM_CODEC, EchoStage::notReady,
            ComponentSerialization.STREAM_CODEC, EchoStage::ready,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), EchoStage::completed,
            ByteBufCodecs.STRING_UTF8, EchoStage::requiredGameStage,
            ShopData.STREAM_CODEC.apply(ByteBufCodecs.list()), EchoStage::shopUnlocked,
            ByteBufCodecs.optional(StageCompletionReward.STREAM_CODEC), EchoStage::completionReward,
            EchoStage::new
    );

    public Component completionRewardSummary() {
        return completionReward.flatMap(r -> r.description().isEmpty() ? Optional.empty() : Optional.of(r.description().getFirst()))
                .orElse(title);
    }

    public boolean isAutoclaimReward() {
        return completionReward.map(StageCompletionReward::autoclaim).orElse(false);
    }
}
