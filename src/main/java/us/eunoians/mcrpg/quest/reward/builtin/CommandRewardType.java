package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
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
 * display: "Special Reward"           # inline fallback label (MiniMessage)
 * </pre>
 * <p>
 * At grant time each command string is resolved in two passes:
 * <ol>
 *     <li>{@code {player}} is replaced with the player's name (backward-compatible shorthand).</li>
 *     <li>The resulting string is run through PlaceholderAPI if PAPI is installed, so any
 *         {@code %placeholder%} token supported by any registered PAPI expansion is resolved.</li>
 * </ol>
 * Commands are dispatched via the Bukkit console sender — not MiniMessage.
 * Display labels use {@code <variable>} syntax consistent with the localization framework.
 * <p>
 * Display label resolution order:
 * <ol>
 *     <li>Auto-derived quest-scoped / template-scoped localization route</li>
 *     <li>Inline {@code display} field</li>
 *     <li>{@link LocalizationKey#QUEST_REWARD_COMMAND_FALLBACK_DISPLAY} generic fallback</li>
 * </ol>
 */
public class CommandRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "command");

    private final List<String> commands;
    private final String displayLabel;
    private final Route localizationRoute;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public CommandRewardType() {
        this.commands = List.of();
        this.displayLabel = "";
        this.localizationRoute = null;
    }

    private CommandRewardType(@NotNull List<String> commands, @NotNull String displayLabel,
                              @Nullable Route localizationRoute) {
        this.commands = List.copyOf(commands);
        this.displayLabel = displayLabel;
        this.localizationRoute = localizationRoute;
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
                section.getString("display", ""),
                null);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public CommandRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        Object raw = config.getOrDefault("commands", List.of());
        List<String> cmds = raw instanceof List<?> ? ((List<String>) raw) : List.of();
        String label = config.getOrDefault("display", "").toString();
        Route route = config.containsKey("localization-route")
                ? Route.fromString(config.get("localization-route").toString())
                : null;
        return new CommandRewardType(cmds, label, route);
    }

    @NotNull
    @Override
    public CommandRewardType withLocalizationRoute(@NotNull Route route) {
        return new CommandRewardType(commands, displayLabel, route);
    }

    @Override
    public void grant(@NotNull Player player) {
        for (String command : commands) {
            String resolved = McRPGMethods.applyPapi(command.replace("{player}", player.getName()), player);
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
    public String describeForDisplay(@NotNull McRPGPlayer player) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        if (localizationRoute != null) {
            try {
                return localization.getLocalizedMessage(player, localizationRoute);
            } catch (Exception ignored) {
                // Fall through to inline display label
            }
        }
        if (!displayLabel.isEmpty()) {
            return displayLabel;
        }
        try {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_COMMAND_FALLBACK_DISPLAY);
        } catch (Exception ignored) {
            return describeForDisplay();
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("commands", commands);
        if (!displayLabel.isEmpty()) {
            map.put("display", displayLabel);
        }
        if (localizationRoute != null) {
            map.put("localization-route", localizationRoute.join('.'));
        }
        return map;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
