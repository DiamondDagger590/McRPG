package us.eunoians.mcrpg.command.quest.parser;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.NamespacedKey;
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
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.Optional;

/**
 * Cloud argument parser that resolves a {@link QuestDefinition} from the
 * {@link QuestDefinitionRegistry} by its {@link NamespacedKey} string.
 * Tab-completes all registered quest definition keys.
 */
public class QuestDefinitionParser implements ArgumentParser<CommandSourceStack, QuestDefinition>,
        BlockingSuggestionProvider.Strings<CommandSourceStack> {

    /**
     * Creates a parser descriptor for this parser.
     *
     * @return the parser descriptor
     */
    @NotNull
    public static ParserDescriptor<CommandSourceStack, QuestDefinition> questDefinitionParser() {
        return ParserDescriptor.of(new QuestDefinitionParser(), QuestDefinition.class);
    }

    @Override
    @NotNull
    public ArgumentParseResult<QuestDefinition> parse(@NotNull CommandContext<CommandSourceStack> context,
                                                       @NotNull CommandInput input) {
        String raw = input.peekString();
        NamespacedKey key = NamespacedKey.fromString(raw);
        if (key == null) {
            return ArgumentParseResult.failure(new QuestDefinitionParseException(raw, context));
        }

        QuestDefinitionRegistry registry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_DEFINITION);
        Optional<QuestDefinition> definition = registry.get(key);
        if (definition.isPresent()) {
            input.readString();
            return ArgumentParseResult.success(definition.get());
        }
        return ArgumentParseResult.failure(new QuestDefinitionParseException(raw, context));
    }

    @Override
    @NotNull
    public Iterable<String> stringSuggestions(@NotNull CommandContext<CommandSourceStack> context,
                                              @NotNull CommandInput input) {
        QuestDefinitionRegistry registry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_DEFINITION);
        return registry.getRegisteredKeys().stream()
                .map(NamespacedKey::toString)
                .toList();
    }

    private static class QuestDefinitionParseException extends ParserException {

        private final String input;

        public QuestDefinitionParseException(@NotNull String input, @NotNull CommandContext<?> context) {
            super(
                    QuestDefinitionParser.class,
                    context,
                    Caption.of("argument.parse.failure.quest_definition"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        @NotNull
        public String input() {
            return input;
        }
    }
}
