package dev.ftb.mods.ftbechoes.net;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.util.TeamStages;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record SyncTeamStageMessage(Collection<String> stages, boolean adding) implements CustomPacketPayload {
    public static final Type<SyncTeamStageMessage> TYPE = new Type<>(FTBEchoes.id("sync_team_stage"));
    public static final StreamCodec<FriendlyByteBuf, SyncTeamStageMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)), SyncTeamStageMessage::stages,
            ByteBufCodecs.BOOL, SyncTeamStageMessage::adding,
            SyncTeamStageMessage::new
    );

    public static SyncTeamStageMessage add(String tag) {
        return new SyncTeamStageMessage(List.of(tag), true);
    }

    public static SyncTeamStageMessage add(Collection<String> tags) {
        return new SyncTeamStageMessage(tags, true);
    }

    public static SyncTeamStageMessage remove(String tag) {
        return new SyncTeamStageMessage(List.of(tag), false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(SyncTeamStageMessage message, IPayloadContext ignored) {
        Team clientTeam = FTBTeamsAPI.api().getClientManager().selfTeam();
        if (message.adding()) {
            message.stages.forEach(stage -> TeamStages.addTeamStage(clientTeam, stage));
        } else {
            message.stages.forEach(stage -> TeamStages.removeTeamStage(clientTeam, stage));
        }
    }
}
