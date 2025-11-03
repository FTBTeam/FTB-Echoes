package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Will be replaced by SyncTeamStageMessage soon
@Deprecated
public record SyncGameStageMessage(Collection<String> stages, boolean adding) implements CustomPacketPayload {
    public static final Type<SyncGameStageMessage> TYPE = new Type<>(FTBEchoes.id("sync_game_stage"));
    public static final StreamCodec<FriendlyByteBuf, SyncGameStageMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)), SyncGameStageMessage::stages,
            ByteBufCodecs.BOOL, SyncGameStageMessage::adding,
            SyncGameStageMessage::new
    );

    public static SyncGameStageMessage add(String tag) {
        return new SyncGameStageMessage(List.of(tag), true);
    }

    public static SyncGameStageMessage add(Collection<String> tags) {
        return new SyncGameStageMessage(tags, true);
    }

    public static SyncGameStageMessage remove(String tag) {
        return new SyncGameStageMessage(List.of(tag), false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(SyncGameStageMessage message, IPayloadContext context) {
        if (message.adding()) {
            message.stages().forEach(s -> context.player().getTags().add(s));
        } else {
            message.stages().forEach(s -> context.player().getTags().remove(s));
        }
    }
}
