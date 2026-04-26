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
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A variant of {@link CommandRewardType} that supports an {@code {amount}} placeholder
 * in the command template. When used in a split-mode distribution tier, the
 * {@code {amount}} token is replaced with the scaled amount at grant time.
 * <p>
 * Config format:
 * <pre>
 * type: mcrpg:scalable_command
 * command: "give {player} diamond {amount}"
 * base-amount: 10
 * display: "Diamond Reward"           # inline fallback label (MiniMessage)
 * </pre>
 * <p>
 * At grant time the command string is resolved in two passes:
 * <ol>
 *     <li>{@code {player}} and {@code {amount}} are replaced with the player's name and the
 *         (potentially scaled) amount respectively.</li>
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
 *     <li>{@link LocalizationKey#QUEST_REWARD_SCALABLE_COMMAND_FALLBACK_DISPLAY} generic fallback</li>
 * </ol>
 */
public final class ScalableCommandRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "scalable_command");

    private final String commandTemplate;
    private final long baseAmount;
    private final String displayLabel;
    private final Route localizationRoute;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public ScalableCommandRewardType() {
        this.commandTemplate = "";
        this.baseAmount = 0;
        this.displayLabel = "";
        this.localizationRoute = null;
    }

    private ScalableCommandRewardType(@NotNull String commandTemplate, long baseAmount,
                                      @NotNull String displayLabel,
                                      @Nullable Route localizationRoute) {
        this.commandTemplate = commandTemplate;
        this.baseAmount = baseAmount;
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
    public String describeForDisplay() {
        if (!displayLabel.isEmpty()) {
            return displayLabel + " (x" + baseAmount + ")";
        }
        return "Scaled Reward (x" + baseAmount + ")";
    }

    @NotNull
    @Override
    public String describeForDisplay(@NotNull McRPGPlayer player) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String amountSuffix = resolveAmountSuffix(localization, player);
        if (localizationRoute != null) {
            try {
                return localization.getLocalizedMessage(player, localizationRoute) + amountSuffix;
            } catch (Exception ignored) {
                // Fall through to inline display label
            }
        }
        if (!displayLabel.isEmpty()) {
            return displayLabel + amountSuffix;
        }
        try {
            return localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_REWARD_SCALABLE_COMMAND_FALLBACK_DISPLAY) + amountSuffix;
        } catch (Exception ignored) {
            return describeForDisplay();
        }
    }

    /**
     * Resolves the localized amount suffix (e.g. {@code " (x10)"}) for display.
     * Falls back to an inline English string if the locale key is missing.
     */
    @NotNull
    private String resolveAmountSuffix(@NotNull McRPGLocalizationManager localization,
                                       @NotNull McRPGPlayer player) {
        try {
            return localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_SCALABLE_AMOUNT_SUFFIX,
                    Map.of("amount", String.valueOf(baseAmount)));
        } catch (Exception ignored) {
            return " (x" + baseAmount + ")";
        }
    }

    @NotNull
    @Override
    public QuestRewardType withAmountMultiplier(double multiplier) {
        long scaled = Math.max(1, Math.round(baseAmount * multiplier));
        return new ScalableCommandRewardType(commandTemplate, scaled, displayLabel, localizationRoute);
    }

    @NotNull
    @Override
    public OptionalLong getNumericAmount() {
        return OptionalLong.of(baseAmount);
    }

    @Override
    public void grant(@NotNull Player player) {
        if (commandTemplate.isEmpty()) {
            return;
        }
        String resolved = McRPGMethods.applyPapi(
                commandTemplate
                        .replace("{player}", player.getName())
                        .replace("{amount}", String.valueOf(baseAmount)),
                player);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
    }

    @NotNull
    @Override
    public ScalableCommandRewardType parseConfig(@NotNull Section section) {
        return new ScalableCommandRewardType(
                section.getString("command", ""),
                section.getLong("base-amount", 0L),
                section.getString("display", ""),
                null);
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("command", commandTemplate);
        map.put("base-amount", baseAmount);
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
    public ScalableCommandRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String cmd = config.getOrDefault("command", "").toString();
        long amt = config.containsKey("base-amount") ? ((Number) config.get("base-amount")).longValue() : 0;
        String label = config.getOrDefault("display", "").toString();
        Route route = config.containsKey("localization-route")
                ? Route.fromString(config.get("localization-route").toString())
                : null;
        return new ScalableCommandRewardType(cmd, amt, label, route);
    }

    @NotNull
    @Override
    public ScalableCommandRewardType withLocalizationRoute(@NotNull Route route) {
        return new ScalableCommandRewardType(commandTemplate, baseAmount, displayLabel, route);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
