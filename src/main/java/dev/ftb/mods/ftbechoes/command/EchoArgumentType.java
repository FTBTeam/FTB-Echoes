package dev.ftb.mods.ftbechoes.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class EchoArgumentType implements ArgumentType<Echo> {
    private static final DynamicCommandExceptionType INVALID_ECHO = new DynamicCommandExceptionType(
            (id) -> Component.translatable("ftbechoes.commands.invalid_echo", id));
    private static final DynamicCommandExceptionType UNKNOWN_ECHO = new DynamicCommandExceptionType(
            (id) -> Component.translatable("ftbechoes.commands.unknown_echo", id));

    @Override
    public Echo parse(StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        String s = readResourceLocation(reader);
        try {
            return EchoManager.getInstance().getEcho(ResourceLocation.parse(s))
                    .orElseThrow(() -> UNKNOWN_ECHO.create(s));
        } catch (ResourceLocationException e) {
            reader.setCursor(start);
            throw INVALID_ECHO.create(s);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var echoes = EchoManager.getInstance().getEchoes();
        return SharedSuggestionProvider.suggest(echoes.stream().map(e -> e.id().toString()), builder);
    }

    public static EchoArgumentType echo() {
        return new EchoArgumentType();
    }

    public static Echo get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, Echo.class);
    }

    public static String readResourceLocation(StringReader reader) {
        final int start = reader.getCursor();
        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }
}
