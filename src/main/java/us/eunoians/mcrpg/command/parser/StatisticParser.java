package us.eunoians.mcrpg.command.parser;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
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

/**
 * Cloud command parser for {@link Statistic} arguments. Parses against the display names
 * of all registered statistics and provides tab completion.
 */
public class StatisticParser implements ArgumentParser<CommandSourceStack, Statistic>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    /**
     * Creates a {@link ParserDescriptor} for this parser.
     *
     * @return A parser descriptor.
     */
    public static @NotNull ParserDescriptor<CommandSourceStack, Statistic> statisticParser() {
        return ParserDescriptor.of(new StatisticParser(), Statistic.class);
    }

    @Override
    public @NotNull ArgumentParseResult<Statistic> parse(
            @NotNull CommandContext<CommandSourceStack> commandContext,
            @NotNull CommandInput commandInput) {
        String input = commandInput.peekString();
        StatisticRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
        for (Statistic statistic : registry.getRegisteredStatistics()) {
            if (statistic.getDisplayName().equalsIgnoreCase(input)) {
                commandInput.readString();
                return ArgumentParseResult.success(statistic);
            }
        }
        return ArgumentParseResult.failure(new StatisticParseException(input, commandContext));
    }

    @Override
    public @NotNull Iterable<String> stringSuggestions(
            @NotNull CommandContext<CommandSourceStack> commandContext,
            @NotNull CommandInput input) {
        StatisticRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
        return registry.getRegisteredStatistics().stream()
                .map(Statistic::getDisplayName)
                .map(String::toLowerCase)
                .toList();
    }

    private static class StatisticParseException extends ParserException {

        private final String input;

        public StatisticParseException(final @NotNull String input, final @NotNull CommandContext<?> context) {
            super(
                    StatisticParser.class,
                    context,
                    Caption.of("argument.parse.failure.statistic"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public String input() {
            return this.input;
        }
    }
}
