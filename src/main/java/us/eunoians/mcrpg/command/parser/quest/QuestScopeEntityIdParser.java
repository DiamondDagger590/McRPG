package us.eunoians.mcrpg.command.parser.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
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
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a scoped entity ID argument (e.g., a land name).
 * Provides tab-completion from all active entities known to registered
 * {@link ScopedBoardAdapter} instances.
 */
public class QuestScopeEntityIdParser implements ArgumentParser<CommandSourceStack, String>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    public static @NotNull ParserDescriptor<CommandSourceStack, String> entityIdParser() {
        return ParserDescriptor.of(new QuestScopeEntityIdParser(), String.class);
    }

    @Override
    public @NotNull ArgumentParseResult<String> parse(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull CommandInput commandInput) {
        String input = commandInput.peekString();
        if (input.isEmpty()) {
            return ArgumentParseResult.failure(new EntityIdParseException(input, commandContext));
        }
        commandInput.readString();
        return ArgumentParseResult.success(input);
    }

    @Override
    public @NotNull Iterable<String> stringSuggestions(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull CommandInput input) {
        ScopedBoardAdapterRegistry adapterRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);

        List<String> suggestions = new ArrayList<>();
        for (ScopedBoardAdapter adapter : adapterRegistry.getAll()) {
            suggestions.addAll(adapter.getAllActiveEntities());
        }
        return suggestions;
    }

    private static class EntityIdParseException extends ParserException {

        private final String input;

        public EntityIdParseException(final @NotNull String input, final @NotNull CommandContext<?> context) {
            super(
                    QuestScopeEntityIdParser.class,
                    context,
                    Caption.of("argument.parse.failure.entity_id"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public String input() {
            return this.input;
        }
    }
}
