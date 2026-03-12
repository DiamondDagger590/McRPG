package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
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
    private final String displayLabel;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public CommandRewardType() {
        this.commands = List.of();
        this.displayLabel = "";
    }

    private CommandRewardType(@NotNull List<String> commands, @NotNull String displayLabel) {
        this.commands = List.copyOf(commands);
        this.displayLabel = displayLabel;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public CommandRewardType parseConfig(@NotNull Section section) {
        return new CommandRewardType(
                section.getStringList("commands"),
                section.getString("display", ""));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public CommandRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        Object raw = config.getOrDefault("commands", List.of());
        List<String> cmds = raw instanceof List<?> ? ((List<String>) raw) : List.of();
        String label = config.getOrDefault("display", "").toString();
        return new CommandRewardType(cmds, label);
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
    public String describeForDisplay() {
        return displayLabel.isEmpty() ? "Special Reward" : displayLabel;
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("commands", commands);
        if (!displayLabel.isEmpty()) {
            map.put("display", displayLabel);
        }
        return map;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
