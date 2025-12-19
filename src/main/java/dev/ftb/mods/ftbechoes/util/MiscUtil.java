package dev.ftb.mods.ftbechoes.util;

import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamStagesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class MiscUtil {
    public static @NotNull Component formatCost(int cost) {
        return Component.empty().append(Component.literal("⬤ ").withStyle(ChatFormatting.YELLOW)).append(String.valueOf(cost)).withStyle(ChatFormatting.DARK_GREEN);
    }

    public static boolean hasStage(Player player, Team team, String stage) {
        // player stage first, then team stage
        // we will probably migrate to team then player later, or simply team stage only
        return StageHelper.getInstance().getProvider().has(player, stage)
                || TeamStagesHelper.hasTeamStage(team, stage);
    }
}
