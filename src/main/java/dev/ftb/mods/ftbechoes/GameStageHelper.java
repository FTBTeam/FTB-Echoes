package dev.ftb.mods.ftbechoes;

import net.minecraft.world.entity.player.Player;

public class GameStageHelper {
    public static boolean hasStage(Player player, String gameStage) {
        // TODO kubejs integration?
        return player.getTags().contains(gameStage);
    }
}
