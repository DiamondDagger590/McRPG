package us.eunoians.mcrpg.command.admin.chain;

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
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.Optional;

/**
 * Cloud argument parser that resolves a {@link QuestChainDefinition} from the
 * {@link QuestChainRegistry} by its {@link NamespacedKey} string.
 * Tab-completes all registered chain keys.
 */
public class ChainKeyParser implements ArgumentParser<CommandSourceStack, QuestChainDefinition>,
        BlockingSuggestionProvider.Strings<CommandSourceStack> {

    /**
     * Creates a parser descriptor for this parser.
     *
     * @return the parser descriptor
     */
    @NotNull
    public static ParserDescriptor<CommandSourceStack, QuestChainDefinition> chainKeyParser() {
        return ParserDescriptor.of(new ChainKeyParser(), QuestChainDefinition.class);
    }

    @Override
    @NotNull
    public ArgumentParseResult<QuestChainDefinition> parse(@NotNull CommandContext<CommandSourceStack> context,
                                                           @NotNull CommandInput input) {
        String raw = input.peekString();
        NamespacedKey key = NamespacedKey.fromString(raw);
        if (key == null) {
            return ArgumentParseResult.failure(new ChainKeyParseException(raw, context));
        }

        QuestChainRegistry registry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);
        Optional<QuestChainDefinition> chain = registry.get(key);
        if (chain.isPresent()) {
            input.readString();
            return ArgumentParseResult.success(chain.get());
        }
        return ArgumentParseResult.failure(new ChainKeyParseException(raw, context));
    }

    @Override
    @NotNull
    public Iterable<String> stringSuggestions(@NotNull CommandContext<CommandSourceStack> context,
                                              @NotNull CommandInput input) {
        QuestChainRegistry registry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);
        return registry.allChains().stream()
                .map(chain -> chain.getChainKey().toString())
                .toList();
    }

    /**
     * Thrown when a provided string cannot be resolved to a registered chain definition.
     */
    private static class ChainKeyParseException extends ParserException {

        public ChainKeyParseException(@NotNull String input, @NotNull CommandContext<?> context) {
            super(
                    ChainKeyParser.class,
                    context,
                    Caption.of("argument.parse.failure.chain_key"),
                    CaptionVariable.of("input", input)
            );
        }
    }
}
