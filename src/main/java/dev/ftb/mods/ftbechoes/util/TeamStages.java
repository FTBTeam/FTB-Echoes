package dev.ftb.mods.ftbechoes.util;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.UUID;

public class TeamStages {
    private static final String ECHOES_TEAMSTAGES = "ftbechoes:team_stages";

    public static boolean addTeamStage(UUID teamId, String stage) {
        return FTBTeamsAPI.api().getManager().getTeamByID(teamId)
                .map(team -> addTeamStage(team, stage)).orElse(false);
    }

    public static boolean removeTeamStage(UUID teamId, String stage) {
        return FTBTeamsAPI.api().getManager().getTeamByID(teamId)
                .map(team -> removeTeamStage(team, stage)).orElse(false);
    }

    public static boolean hasTeamStage(UUID teamId, String stage) {
        return FTBTeamsAPI.api().getManager().getTeamByID(teamId)
                .map(team -> hasTeamStage(team, stage)).orElse(false);
    }

    public static boolean addTeamStage(Team team, String stage) {
        return updateStage(team, stage, true);
    }

    public static boolean removeTeamStage(Team team, String stage) {
        return updateStage(team, stage, false);
    }

    public static boolean hasTeamStage(Team team, String stage) {
        return team.getExtraData().getCompound(ECHOES_TEAMSTAGES).contains(stage, Tag.TAG_STRING);
    }

    private static boolean updateStage(Team team, String stage, boolean adding) {
        if (adding == hasTeamStage(team, stage)) {
            return false;
        }

        CompoundTag root = team.getExtraData();
        if (!root.contains(ECHOES_TEAMSTAGES, Tag.TAG_COMPOUND)) {
            root.put(ECHOES_TEAMSTAGES, new CompoundTag());
        }
        CompoundTag stages = root.getCompound(ECHOES_TEAMSTAGES);
        if (adding) {
            stages.putBoolean(stage, true);
        } else {
            stages.remove(stage);
        }
        team.markDirty();
        return true;
    }
}
