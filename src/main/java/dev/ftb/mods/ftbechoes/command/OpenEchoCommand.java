package dev.ftb.mods.ftbechoes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.EchoPage;
import dev.ftb.mods.ftbechoes.net.OpenEchoMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class OpenEchoCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("open")
                .requires(ctx -> ctx.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(argument("echo", EchoArgumentType.echo())
                        .suggests((ctx, builder) -> suggestEchoIds(builder))
                        .executes(ctx -> openEcho(ctx, EchoArgumentType.get(ctx, "echo"), null))
                        .then(argument("page", PageArgumentType.page())
                                .executes(ctx -> openEcho(ctx, EchoArgumentType.get(ctx, "echo"), PageArgumentType.getPage(ctx, "page")))
                        )
                );
    }

    private static int openEcho(CommandContext<CommandSourceStack> ctx, Echo echo, @Nullable EchoPage page) throws CommandSyntaxException {
        PacketDistributor.sendToPlayer(ctx.getSource().getPlayerOrException(), new OpenEchoMessage(echo.id(), Optional.ofNullable(page)));

        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> suggestEchoIds(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(EchoManager.getInstance().getEchoes().stream().map(e -> e.id().toString()).toList(), builder);
    }
}
