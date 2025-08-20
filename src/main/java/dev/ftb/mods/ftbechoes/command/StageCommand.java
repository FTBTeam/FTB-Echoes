package dev.ftb.mods.ftbechoes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftbechoes.net.SyncGameStageMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class StageCommand {
    private static final SimpleCommandExceptionType ERROR_ADD_FAILED
            = new SimpleCommandExceptionType(Component.translatable("commands.tag.add.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED
            = new SimpleCommandExceptionType(Component.translatable("commands.tag.remove.failed"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("gamestage")
                .requires(ctx -> ctx.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(argument("player", EntityArgument.player())
                        .then(literal("add")
                                .then(argument("stage", StringArgumentType.string())
                                        .executes(ctx -> addStage(ctx,
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "stage"),
                                                true)
                                        )
                                )
                        )
                        .then(literal("remove")
                                .then(argument("stage", StringArgumentType.string())
                                        .executes(ctx -> addStage(ctx,
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "stage"),
                                                false)
                                        )
                                )
                        )
                );
    }

    private static int addStage(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String stage, boolean adding) throws CommandSyntaxException {
        if (adding) {
            player.removeTag(stage);
            if (!player.addTag(stage)) {
                throw ERROR_ADD_FAILED.create();
            }
        } else {
            if (!player.removeTag(stage)) {
                throw ERROR_REMOVE_FAILED.create();
            }
        }

        PacketDistributor.sendToPlayer(player, adding ? SyncGameStageMessage.add(stage) : SyncGameStageMessage.remove(stage));

        return Command.SINGLE_SUCCESS;
    }
}
