package us.eunoians.mcrpg.command.parser.quest;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Parses a scope type argument (e.g., "player" or "entity").
 * Provides tab-completion for known scope types.
 */
public class QuestScopeTypeParser implements ArgumentParser<CommandSourceStack, String>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    private static final Set<String> KNOWN_SCOPE_TYPES = Set.of("player", "entity");

    public static @NotNull ParserDescriptor<CommandSourceStack, String> scopeTypeParser() {
        return ParserDescriptor.of(new QuestScopeTypeParser(), String.class);
    }

    @Override
    public @NotNull ArgumentParseResult<String> parse(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull CommandInput commandInput) {
        String input = commandInput.peekString();
        if (!KNOWN_SCOPE_TYPES.contains(input.toLowerCase())) {
            return ArgumentParseResult.failure(new ScopeTypeParseException(input, commandContext));
        }
        commandInput.readString();
        return ArgumentParseResult.success(input.toLowerCase());
    }

    @Override
    public @NotNull Iterable<String> stringSuggestions(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull CommandInput input) {
        return List.copyOf(KNOWN_SCOPE_TYPES);
    }

    private static class ScopeTypeParseException extends ParserException {

        private final String input;

        public ScopeTypeParseException(final @NotNull String input, final @NotNull CommandContext<?> context) {
            super(
                    QuestScopeTypeParser.class,
                    context,
                    Caption.of("argument.parse.failure.scope_type"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public String input() {
            return this.input;
        }
    }
}
