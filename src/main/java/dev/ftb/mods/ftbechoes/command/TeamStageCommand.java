package dev.ftb.mods.ftbechoes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftbechoes.net.SyncTeamStageMessage;
import dev.ftb.mods.ftbechoes.util.TeamStages;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TeamStageCommand {
    private static final SimpleCommandExceptionType ERROR_ADD_FAILED
            = new SimpleCommandExceptionType(Component.translatable("commands.tag.add.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED
            = new SimpleCommandExceptionType(Component.translatable("commands.tag.remove.failed"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("teamstage")
                .requires(ctx -> ctx.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(argument("player", EntityArgument.player())
                        .then(literal("add")
                                .then(argument("stage", StringArgumentType.string())
                                        .executes(ctx -> updateStage(ctx,
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "stage"),
                                                true)
                                        )
                                )
                        )
                        .then(literal("remove")
                                .then(argument("stage", StringArgumentType.string())
                                        .executes(ctx -> updateStage(ctx,
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "stage"),
                                                false)
                                        )
                                )
                        )
                );
    }

    private static int updateStage(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String stage, boolean adding) throws CommandSyntaxException {
        Team team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                .orElseThrow(() -> TeamArgument.TEAM_NOT_FOUND.create(player.getDisplayName()));

        if (adding) {
            if (TeamStages.addTeamStage(team.getTeamId(), stage)) {
                team.getOnlineMembers().forEach(member -> PacketDistributor.sendToPlayer(member, SyncTeamStageMessage.add(stage)));
                ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.added_stage", stage), false);
            }
        } else if (TeamStages.removeTeamStage(team.getTeamId(), stage)) {
            team.getOnlineMembers().forEach(member -> PacketDistributor.sendToPlayer(member, SyncTeamStageMessage.remove(stage)));
            ctx.getSource().sendSuccess(() -> Component.translatable("ftbechoes.commands.removed_stage", stage), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
