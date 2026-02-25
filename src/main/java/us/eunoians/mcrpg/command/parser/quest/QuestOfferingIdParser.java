package us.eunoians.mcrpg.command.parser.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
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
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.UUID;

/**
 * Parses a UUID string representing a {@link BoardOffering} ID.
 * Provides tab-completion from the current set of visible shared offerings.
 */
public class QuestOfferingIdParser implements ArgumentParser<CommandSourceStack, UUID>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    private static final NamespacedKey DEFAULT_BOARD_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board");

    public static @NotNull ParserDescriptor<CommandSourceStack, UUID> offeringIdParser() {
        return ParserDescriptor.of(new QuestOfferingIdParser(), UUID.class);
    }

    @Override
    public @NotNull ArgumentParseResult<UUID> parse(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull CommandInput commandInput) {
        String input = commandInput.peekString();
        try {
            UUID uuid = UUID.fromString(input);
            commandInput.readString();
            return ArgumentParseResult.success(uuid);
        } catch (IllegalArgumentException e) {
            return ArgumentParseResult.failure(new OfferingIdParseException(input, commandContext));
        }
    }

    @Override
    public @NotNull Iterable<String> stringSuggestions(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull CommandInput input) {
        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);

        List<BoardOffering> offerings = boardManager.getSharedOfferingsForBoard(DEFAULT_BOARD_KEY);
        return offerings.stream()
                .map(o -> o.getOfferingId().toString())
                .toList();
    }

    private static class OfferingIdParseException extends ParserException {

        private final String input;

        public OfferingIdParseException(final @NotNull String input, final @NotNull CommandContext<?> context) {
            super(
                    QuestOfferingIdParser.class,
                    context,
                    Caption.of("argument.parse.failure.offering_id"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public String input() {
            return this.input;
        }
    }
}
