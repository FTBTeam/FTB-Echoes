package dev.ftb.mods.ftbechoes.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbechoes.net.OpenTeamProgressInfoScreenMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.minecraft.commands.Commands.literal;

public class ProgressInfoCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("progressinfo")
                .requires(ctx -> ctx.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ProgressInfoCommand::openGui);
    }

    private static int openGui(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PacketDistributor.sendToPlayer(ctx.getSource().getPlayerOrException(), new OpenTeamProgressInfoScreenMessage());
        return 0;
    }
}
