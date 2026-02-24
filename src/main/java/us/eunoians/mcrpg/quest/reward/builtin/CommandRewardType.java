package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Built-in reward type that executes commands as the console when granted.
 * <p>
 * Config format:
 * <pre>
 * type: mcrpg:command
 * commands:
 *   - "give {player} diamond 5"
 *   - "broadcast {player} completed a quest!"
 * </pre>
 * <p>
 * The placeholder {@code {player}} is replaced with the player's name.
 * TODO: Migrate to PAPI (PlaceholderAPI via McCore) for placeholder resolution.
 */
public class CommandRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "command");

    private final List<String> commands;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public CommandRewardType() {
        this.commands = List.of();
    }

    private CommandRewardType(@NotNull List<String> commands) {
        this.commands = List.copyOf(commands);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public CommandRewardType parseConfig(@NotNull Section section) {
        return new CommandRewardType(section.getStringList("commands"));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public CommandRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        Object raw = config.getOrDefault("commands", List.of());
        List<String> cmds = raw instanceof List<?> ? ((List<String>) raw) : List.of();
        return new CommandRewardType(cmds);
    }

    @Override
    public void grant(@NotNull Player player) {
        for (String command : commands) {
            String resolved = command.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        return Map.of("commands", commands);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
