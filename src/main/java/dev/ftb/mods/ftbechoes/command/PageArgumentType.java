package dev.ftb.mods.ftbechoes.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbechoes.echo.EchoPage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class PageArgumentType implements ArgumentType<EchoPage> {
    private static final EchoPage[] VALUES = EchoPage.values();
    private static final DynamicCommandExceptionType UNKNOWN_PAGE
            = new DynamicCommandExceptionType(s -> Component.translatableEscape("ftbechoes.commands.unknown_page", s));

    public static PageArgumentType page() {
        return new PageArgumentType();
    }

    @Override
    public EchoPage parse(StringReader reader) throws CommandSyntaxException {
        String s = reader.readUnquotedString();
        EchoPage gametype = EchoPage.NAME_MAP.getNullable(s);
        if (gametype == null) {
            throw UNKNOWN_PAGE.createWithContext(reader, s);
        } else {
            return gametype;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider ?
                SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(EchoPage::getSerializedName), builder) :
                Suggestions.empty();
    }

    public static EchoPage getPage(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, EchoPage.class);
    }
}
