package dev.ftb.mods.ftbechoes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgressManager;
import dev.ftb.mods.ftblibrary.FTBLibraryCommands;
import dev.ftb.mods.ftblibrary.net.EditNBTPacket;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Util;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class NBTEditCommand {
    public static final Identifier FTBECHOES_PROGRESS = FTBEchoes.id("progress");

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("nbtedit")
                .requires(ctx -> ctx.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(argument("player", EntityArgument.player())
                        .executes(ctx -> doEdit(ctx, EntityArgument.getPlayer(ctx, "player")))
                );
    }

    private static int doEdit(CommandContext<CommandSourceStack> ctx, ServerPlayer targetPlayer) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        return TeamProgressManager.get(src.getServer()).getProgress(player).map(progress -> {
            var nbtOps = ctx.getSource().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            CompoundTag info = Util.make(new CompoundTag(), t -> {
                Component teamName = FTBTeamsAPI.api().getManager().getTeamForPlayer(targetPlayer)
                        .map(Team::getColoredName)
                        .orElse(Component.literal("<unknown>"));
                MutableComponent title = Component.literal("Echo Progress for ").append(teamName);
                t.store("title", ComponentSerialization.CODEC, nbtOps, title);
                t.putString("type", FTBECHOES_PROGRESS.toString());
                t.store("id", UUIDUtil.CODEC, targetPlayer.getUUID());
                t.put("text", FTBLibraryCommands.InfoBuilder.create(ctx)
                        .add("Team ID", teamName)
                        .build()
                );
            });
            return TeamProgress.CODEC.encodeStart(NbtOps.INSTANCE, progress)
                    .resultOrPartial(err -> src.sendFailure(Component.literal(err).withStyle(ChatFormatting.RED)))
                    .map(tag -> {
                        // tag should always be a compound tag!
                        Server2PlayNetworking.send(player, new EditNBTPacket(info, (CompoundTag) tag));
                        return Command.SINGLE_SUCCESS;
                    })
                    .orElse(0);
        }).orElse(0);
    }

    public static void handleResponse(ServerPlayer serverPlayer, CompoundTag info, CompoundTag data) {
        info.read("id", UUIDUtil.CODEC).flatMap(id -> FTBTeamsAPI.api().getManager().getTeamForPlayerID(id))
                .ifPresent(team -> TeamProgress.CODEC.parse(NbtOps.INSTANCE, data)
                        .ifSuccess(progress -> {
                            serverPlayer.sendSystemMessage(Component.translatable("ftbechoes.message.progress_edited",
                                    team.getColoredName()));
                            TeamProgressManager.get(serverPlayer.level().getServer()).injectProgressData(team.getTeamId(), progress);
                        }));
    }
}
