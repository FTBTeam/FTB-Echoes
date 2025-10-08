package dev.ftb.mods.ftbechoes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import net.minecraft.ChatFormatting;
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
                                        .then(literal("set_stage")
                                                .then(argument("stage_idx", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> setProgress(ctx,
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                EchoArgumentType.get(ctx, "echo"),
                                                                IntegerArgumentType.getInteger(ctx, "stage_idx")
                                                        ))
                                                )
                                        )
                                        .then(literal("reset_reward")
                                                .executes(ctx -> resetRewardClaimed(ctx,
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        EchoArgumentType.get(ctx, "echo"), -1)
                                                )
                                                .then(argument("stage_idx", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> resetRewardClaimed(ctx,
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                EchoArgumentType.get(ctx, "echo"),
                                                                IntegerArgumentType.getInteger(ctx, "stage_idx")
                                                        ))
                                                )
                                        )
                                )
                        )
                )
                .then(literal("team")
                        .then(argument("team", TeamArgument.create())
                                .then(argument("echo", EchoArgumentType.echo())
                                        .then(argument("stage", IntegerArgumentType.integer(0))
                                                .then(literal("set_stage")
                                                        .executes(ctx -> setProgress(ctx,
                                                                TeamArgument.get(ctx, "team"),
                                                                EchoArgumentType.get(ctx, "echo"),
                                                                IntegerArgumentType.getInteger(ctx, "stage")
                                                        ))
                                                )
                                        )
                                )
                        )
                )
                .then(literal("reset-all")
                        .then(literal("player")
                                .then(argument("player", EntityArgument.player())
                                        .executes(ctx -> resetAllProgress(ctx, EntityArgument.getPlayer(ctx, "player")))
                                )
                        )
                        .then(literal("team")
                                .then(argument("team", TeamArgument.create())
                                        .executes(ctx -> resetAllProgress(ctx, TeamArgument.get(ctx, "team")))
                                )
                        )
                )
                .then(literal("reset-stock")
                        .then(literal("player")
                                .then(argument("player", EntityArgument.player())
                                        .then(argument("echo", EchoArgumentType.echo())
                                                .executes(ctx -> resetShopStock(ctx,
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        EchoArgumentType.get(ctx, "echo")
                                                ))
                                        )
                                )
                        )
                        .then(literal("team")
                                .then(argument("team", TeamArgument.create())
                                        .then(argument("echo", EchoArgumentType.echo())
                                                .executes(ctx -> resetShopStock(ctx,
                                                        TeamArgument.get(ctx, "team"),
                                                        EchoArgumentType.get(ctx, "echo")
                                                ))
                                        )
                                )
                        )
                );
    }

    private static int setProgress(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Echo echo, int stageIdx) {
        if (TeamProgressManager.get(ctx.getSource().getServer()).setStage(player, echo.id(), stageIdx)) {
            ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.progress_changed", player.getDisplayName(), echo.id().toString(), stageIdx), false);
            return Command.SINGLE_SUCCESS;
        }

        ctx.getSource().sendFailure(Component.translatable("ftbechoes.commands.progress_changed.failed").withStyle(ChatFormatting.RED));
        return 0;
    }

    private static int setProgress(CommandContext<CommandSourceStack> ctx, Team team, Echo echo, int stageIdx) {
        if (TeamProgressManager.get(ctx.getSource().getServer()).setStage(team, echo.id(), stageIdx)) {
            ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.progress_changed", team.getShortName(), echo.id().toString(), stageIdx), false);
            return Command.SINGLE_SUCCESS;
        }

        ctx.getSource().sendFailure(Component.translatable("ftbechoes.commands.progress_changed.failed").withStyle(ChatFormatting.RED));
        return 0;
    }

    private static int resetRewardClaimed(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Echo echo, int stageIdx) {
        if (stageIdx < 0) {
            if (TeamProgressManager.get(ctx.getSource().getServer()).resetAllRewards(player.getUUID(), echo.id())) {
                ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.reward_reset_all", player.getDisplayName(), echo.id().toString(), stageIdx), false);
                return Command.SINGLE_SUCCESS;
            }
        } else if (TeamProgressManager.get(ctx.getSource().getServer()).resetReward(player.getUUID(), echo.id(), stageIdx)) {
            ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.reward_reset", player.getDisplayName(), echo.id().toString(), stageIdx), false);
            return Command.SINGLE_SUCCESS;
        }

        ctx.getSource().sendFailure(Component.translatable("ftbechoes.commands.reward_reset.failed").withStyle(ChatFormatting.RED));
        return 0;
    }

    private static int resetAllProgress(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        var mgr = TeamProgressManager.get(ctx.getSource().getServer());
        EchoManager.getServerInstance().getEchoes().forEach(echo -> {
            mgr.setStage(player, echo.id(), 0);
            mgr.resetAllRewards(player.getUUID(), echo.id());
        });
        ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.all_progress_reset", player.getDisplayName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetAllProgress(CommandContext<CommandSourceStack> ctx, Team team) {
        var mgr = TeamProgressManager.get(ctx.getSource().getServer());
        EchoManager.getServerInstance().getEchoes().forEach(echo -> {
            mgr.setStage(team, echo.id(), 0);
            team.getMembers().forEach(playerId -> mgr.resetAllRewards(playerId, echo.id()));
        });
        ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.all_progress_reset", team.getColoredName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetShopStock(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Echo echo) {
        if (TeamProgressManager.get(ctx.getSource().getServer()).resetShopStock(player, echo.id())) {
            ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.shop_stock_reset", echo.id().toString()), false);
            return Command.SINGLE_SUCCESS;
        }
        ctx.getSource().sendFailure(Component.translatable("ftbechoes.commands.shop_stock_reset.failed", echo.id().toString()).withStyle(ChatFormatting.RED));
        return 0;
    }

    private static int resetShopStock(CommandContext<CommandSourceStack> ctx, Team team, Echo echo) {
        if (TeamProgressManager.get(ctx.getSource().getServer()).resetShopStock(team, echo.id())) {
            ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.shop_stock_reset", echo.id().toString()), false);
            return Command.SINGLE_SUCCESS;
        }
        ctx.getSource().sendFailure(Component.translatable("ftbechoes.commands.shop_stock_reset.failed", echo.id().toString()).withStyle(ChatFormatting.RED));
        return 0;
    }
}
