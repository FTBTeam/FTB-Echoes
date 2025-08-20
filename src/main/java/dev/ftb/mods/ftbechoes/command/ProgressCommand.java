package dev.ftb.mods.ftbechoes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ProgressCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("progress")
                .requires(ctx -> ctx.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("player")
                        .then(argument("player", EntityArgument.player())
                                .then(argument("echo", EchoArgumentType.echo())
                                        .then(argument("stage", IntegerArgumentType.integer(0))
                                                .executes(ctx -> setProgress(ctx,
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        EchoArgumentType.get(ctx, "echo"),
                                                        IntegerArgumentType.getInteger(ctx, "stage")
                                                ))
                                        )
                                )
                        )
                )
                .then(literal("team")
                        .then(argument("team", TeamArgument.create())
                                .then(argument("echo", EchoArgumentType.echo())
                                        .then(argument("stage", IntegerArgumentType.integer(0))
                                                .executes(ctx -> setProgress(ctx,
                                                        TeamArgument.get(ctx, "team"),
                                                        EchoArgumentType.get(ctx, "echo"),
                                                        IntegerArgumentType.getInteger(ctx, "stage")
                                                ))
                                        )
                                )
                        )
                );
    }

    private static int setProgress(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Echo echo, int stage) {
        if (TeamProgressManager.get(ctx.getSource().getServer()).setStage(player, echo.id(), stage)) {
            ctx.getSource().sendSuccess(() -> Component.literal("progress updated!"), false);
            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }

    private static int setProgress(CommandContext<CommandSourceStack> ctx, Team team, Echo echo, int stage) {
        if (TeamProgressManager.get(ctx.getSource().getServer()).setStage(team, echo.id(), stage)) {
            ctx.getSource().sendSuccess(() -> Component.literal("progress updated!"), false);
            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }
}
