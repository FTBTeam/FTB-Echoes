package dev.ftb.mods.ftbechoes.util;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftblibrary.integration.stages.StageProvider;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamStagesHelper;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StageMigration {
    private static final List<String> STAGES = List.of(
            "echo_ancient_meet",
            "echo_ancient_stage4_task1_check",
            "echo_ancient_stage4_task1_completed",
            "echo_catalyst_meet",
            "echo_catalyst_stage3_task1_check",
            "echo_catalyst_stage3_task1_completed",
            "echo_catalyst_stage3_task2_check",
            "echo_catalyst_stage3_task2_completed",
            "echo_catalyst_stage3_task3_check",
            "echo_catalyst_stage3_task3_completed",
            "echo_catalyst_unlock",
            "echo_chaos_meet",
            "echo_chaos_stage5_task1_check",
            "echo_chaos_stage5_task1_completed",
            "echo_chaos_unlock",
            "echo_enchanter_meet",
            "echo_enchanter_stage2_task1_check",
            "echo_enchanter_stage2_task1_completed",
            "echo_enchanter_stage2_task2_completed",
            "echo_enchanter_unlock",
            "echo_fabricator_meet",
            "echo_fabricator_stage2_task1_check",
            "echo_fabricator_stage2_task1_completed",
            "echo_fabricator_unlock",
            "echo_guidance_interact",
            "echo_guidance_meet",
            "echo_guidance_stage1_final_check",
            "echo_guidance_stage1_final_completed",
            "echo_guidance_stage1_task1_check",
            "echo_guidance_stage1_task1_completed",
            "echo_guidance_stage1_task2_check",
            "echo_guidance_stage1_task2_completed",
            "echo_guidance_stage1_task3_check",
            "echo_guidance_stage1_task3_completed",
            "echo_guidance_stage2_task1_check",
            "echo_guidance_stage2_task1_completed",
            "echo_guidance_stage3_task1_check",
            "echo_guidance_stage3_task1_completed",
            "echo_guidance_stage4_task1_check",
            "echo_guidance_stage4_task1_completed",
            "echo_guidance_stage4_task2_check",
            "echo_guidance_stage4_task2_completed",
            "echo_guidance_stage5_task1_check",
            "echo_guidance_stage5_task1_completed",
            "echo_infernal_meet",
            "echo_infernal_stage3_task1_check",
            "echo_infernal_stage3_task1_completed",
            "echo_infernal_unlock",
            "echo_infinity_meet",
            "echo_infinity_stage5_task1_completed",
            "echo_infinity_unlock",
            "echo_light_bender_meet",
            "echo_light_bender_stage3_task1_check",
            "echo_light_bender_stage3_task1_completed",
            "echo_light_bender_stage3_task2_check",
            "echo_light_bender_stage3_task2_completed",
            "echo_light_bender_unlock",
            "echo_machinist_meet",
            "echo_machinist_stage1_task1_check",
            "echo_machinist_stage1_task1_completed",
            "echo_machinist_stage1_task2_check",
            "echo_machinist_stage1_task2_completed",
            "echo_machinist_unlock",
            "echo_magician_meet",
            "echo_magician_stage1_task1_check",
            "echo_magician_stage1_task1_completed",
            "echo_magician_stage1_task2_check",
            "echo_magician_stage1_task2_completed",
            "echo_magician_unlock",
            "echo_quartermaster_meet",
            "echo_quartermaster_stage1_task1_check",
            "echo_quartermaster_stage1_task1_completed",
            "echo_quartermaster_stage1_task2_check",
            "echo_quartermaster_stage1_task2_completed",
            "echo_quartermaster_unlock",
            "echo_radiance_meet",
            "echo_radiance_stage5_task1_check",
            "echo_radiance_stage5_task1_completed",
            "echo_radiance_unlock",
            "echo_stage1_or_check",
            "echo_stage3_or_check",
            "echo_stage4_or_check",
            "echo_stage5_and_check",
            "echo_twilight_ancients_unlock",
            "echo_twilight_meet",
            "echo_twilight_stage4_task1_check",
            "echo_twilight_stage4_task1_completed",
            "echo_wayfinder_meet",
            "echo_wayfinder_stage2_task1_check",
            "echo_wayfinder_stage2_task1_completed",
            "echo_wayfinder_unlock",
            "echo_wyrmwright_meet",
            "echo_wyrmwright_stage4_task1_check",
            "echo_wyrmwright_stage4_task1_completed",
            "echo_wyrmwright_stage4_task2_check",
            "echo_wyrmwright_stage4_task2_completed",
            "echo_wyrmwright_stage5_task1_check",
            "echo_wyrmwright_stage5_task1_completed",
            "echo_wyrmwright_stage5_task2_check",
            "echo_wyrmwright_stage5_task2_completed",
            "echo_wyrmwright_unlock",
            "home_unlocked",
            "t5_zone_unlocked",
            "world_engine_unlocked",
            "creative_motor_unlock"
    );

    public static void migrateStages(ServerPlayer player, Team team) {
        Collection<String> teamStages = TeamStagesHelper.getStages(team);
        StageProvider provider = FTBEchoes.stageProvider();

        List<String> toAdd = new ArrayList<>();
        for (String stage : STAGES) {
            if (provider.has(player, stage) && !teamStages.contains(stage)) {
                toAdd.add(stage);
            }
        }

        if (!toAdd.isEmpty()) {
            TeamStagesHelper.addTeamStages(team, toAdd);
            toAdd.forEach(stage -> provider.remove(player, stage));
            FTBEchoes.LOGGER.info("Migrated {} game stages from player {} to team {}", toAdd.size(), player.getGameProfile().getName(), team.getShortName());
        }
    }
}
