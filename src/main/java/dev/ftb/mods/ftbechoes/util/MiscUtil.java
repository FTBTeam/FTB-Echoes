package dev.ftb.mods.ftbechoes.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class MiscUtil {
    public static @NotNull Component formatCost(int cost) {
        return Component.empty().append(Component.literal("⬤ ").withStyle(ChatFormatting.YELLOW)).append(String.valueOf(cost)).withStyle(ChatFormatting.DARK_GREEN);
    }
}
