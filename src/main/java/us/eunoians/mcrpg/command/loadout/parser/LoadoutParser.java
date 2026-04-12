package us.eunoians.mcrpg.command.loadout.parser;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Cloud command parser for the {@code /mcrpg loadout set} slot argument.
 * <p>
 * Parsing is greedy — all remaining input tokens are joined with spaces — so that
 * multi-word loadout display names (e.g. {@code "pvp mining"}) can be entered without
 * quoting. The parsed value is the raw {@link String} that the command handler then
 * resolves via {@link LoadoutHolder#resolveLoadout(String)}, preserving the existing
 * three-way resolution logic (slot number → exact name → substring name).
 * <p>
 * Tab completion returns:
 * <ul>
 *   <li>A token for every valid slot number ({@code "1"}, {@code "2"}, …).</li>
 *   <li>The plain-text display name of every loadout the player has explicitly named.</li>
 * </ul>
 * Non-player senders and players whose {@link McRPGPlayer} is not yet loaded receive an
 * empty suggestion list.
 */
public class LoadoutParser implements ArgumentParser<CommandSourceStack, String>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    /**
     * Creates a {@link ParserDescriptor} for this parser.
     *
     * @return A parser descriptor backed by a fresh {@link LoadoutParser}.
     */
    @NotNull
    public static ParserDescriptor<CommandSourceStack, String> loadoutParser() {
        return ParserDescriptor.of(new LoadoutParser(), String.class);
    }

    /**
     * Reads all remaining command input greedily, joining tokens with a single space,
     * so that multi-word loadout display names do not require quoting.
     *
     * @param commandContext The current command context.
     * @param commandInput   The remaining command input.
     * @return A successful parse result containing the full remaining input string.
     */
    @Override
    @NotNull
    public ArgumentParseResult<String> parse(
            @NotNull CommandContext<CommandSourceStack> commandContext,
            @NotNull CommandInput commandInput) {
        StringBuilder result = new StringBuilder(commandInput.readString());
        while (commandInput.hasRemainingInput()) {
            result.append(' ').append(commandInput.readString());
        }
        return ArgumentParseResult.success(result.toString().trim());
    }

    /**
     * Returns tab-completion suggestions for the slot argument.
     * <p>
     * Suggestions are scoped to the sending player's own loadouts: one entry per valid
     * slot number, plus one entry per loadout whose display name has been customised.
     * Non-player senders and players not yet loaded by McRPG receive an empty list.
     *
     * @param commandContext The current command context.
     * @param input          The current command input.
     * @return An iterable of suggestion strings, or an empty list for non-player senders.
     */
    @Override
    @NotNull
    public Iterable<String> stringSuggestions(
            @NotNull CommandContext<CommandSourceStack> commandContext,
            @NotNull CommandInput input) {
        if (!(commandContext.sender().getSender() instanceof Player player)) {
            return List.of();
        }
        var mcRPGPlayerOptional = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());
        if (mcRPGPlayerOptional.isEmpty()) {
            return List.of();
        }
        return computeSuggestions(mcRPGPlayerOptional.get().asSkillHolder());
    }

    /**
     * Builds the list of tab-completion suggestions for a given {@link LoadoutHolder}.
     * <p>
     * Returns one string per valid slot number ({@code "1"}, {@code "2"}, …) followed by
     * the plain-text display name of every loadout the holder has explicitly customised
     * (MiniMessage formatting tags are stripped before the name is added).
     *
     * @param holder The holder whose loadout data drives the suggestions.
     * @return A list of suggestion strings.
     */
    @NotNull
    static List<String> computeSuggestions(@NotNull LoadoutHolder holder) {
        List<String> suggestions = new ArrayList<>();
        IntStream.rangeClosed(1, holder.getMaxLoadoutAmount())
                .mapToObj(Integer::toString)
                .forEach(suggestions::add);
        var plainSerializer = PlainTextComponentSerializer.plainText();
        for (Loadout loadout : holder.getNamedLoadouts()) {
            loadout.getDisplay().getDisplayName()
                    .map(name -> plainSerializer.serialize(McRPG.getInstance().getMiniMessage().deserialize(name)))
                    .ifPresent(suggestions::add);
        }
        return suggestions;
    }
}
