package us.eunoians.mcrpg.command.parser;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.stream.Collectors;

/**
 * A fixed version of <a href="https://github.com/Incendo/cloud-minecraft/blob/f93aee8feda24aa4c15de3982b752e40bda74f6b/cloud-bukkit/src/main/java/org/incendo/cloud/bukkit/parser/PlayerParser.java">PlayerParser</a>
 * until <a href="https://github.com/Incendo/cloud-minecraft/pull/59">a fix pr</a> is merged.
 */
public final class PlayerParser<C> implements ArgumentParser<C, Player>, BlockingSuggestionProvider<C> {

    /**
     * Creates a new player parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    public static <C> @NonNull ParserDescriptor<C, Player> playerParser() {
        return ParserDescriptor.of(new PlayerParser<>(), Player.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #playerParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    public static <C> CommandComponent.@NonNull Builder<C, Player> playerComponent() {
        return CommandComponent.<C, Player>builder().parser(playerParser());
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NonNull ArgumentParseResult<Player> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();

        Player player = Bukkit.getPlayer(input);

        if (player == null) {
            return ArgumentParseResult.failure(new PlayerParseException(input, commandContext));
        }

        return ArgumentParseResult.success(player);
    }

    @Override
    public @NonNull Iterable<@NonNull Suggestion> suggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        final CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> bukkit instanceof Player && ((Player) bukkit).canSee(player))
                .map(Player::getName)
                .map(Suggestion::suggestion)
                .collect(Collectors.toList());
    }


    /**
     * Player parse exception
     */
    public static final class PlayerParseException extends ParserException {

        private final String input;

        /**
         * Construct a new Player parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public PlayerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    PlayerParser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public @NonNull String input() {
            return this.input;
        }
    }
}
