package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.logging.Level;

/**
 * Built-in reward type that grants boosted experience to a player's
 * {@link us.eunoians.mcrpg.entity.player.PlayerExperienceExtras} pool.
 * <p>
 * Config format:
 * <pre>
 * type: mcrpg:boosted_experience
 * amount: 500
 * </pre>
 */
public class BoostedExperienceRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "boosted_experience");

    private final int amount;
    @Nullable
    private final Route localizationRoute;
    @NotNull
    private final String displayLabel;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public BoostedExperienceRewardType() {
        this.amount = 0;
        this.localizationRoute = null;
        this.displayLabel = "";
    }

    private BoostedExperienceRewardType(int amount, @Nullable Route localizationRoute, @NotNull String displayLabel) {
        this.amount = amount;
        this.localizationRoute = localizationRoute;
        this.displayLabel = displayLabel;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public BoostedExperienceRewardType parseConfig(@NotNull Section section) {
        int amount = section.getInt("amount", 0);
        if (amount <= 0) {
            McRPG.getInstance().getLogger().warning(
                    "Reward type '" + KEY + "' configured with amount <= 0 (" + amount + ") — reward will have no effect");
        }
        return new BoostedExperienceRewardType(amount, null, "");
    }

    @Override
    public void grant(@NotNull Player player) {
        if (amount <= 0) {
            return;
        }
        Optional<McRPGPlayer> mcRPGPlayerOpt = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());
        mcRPGPlayerOpt.ifPresent(p -> p.getExperienceExtras().modifyBoostedExperience(amount));
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("amount", amount);
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
    public BoostedExperienceRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        int amt = deserializeAmount(config);
        if (amt <= 0) {
            McRPG.getInstance().getLogger().warning(
                    "Reward type '" + KEY + "' deserialized with amount <= 0 (" + amt + ") — reward will have no effect");
        }
        Route route = config.containsKey("localization-route")
                ? Route.fromString(config.get("localization-route").toString())
                : null;
        String label = config.getOrDefault("display", "").toString();
        return new BoostedExperienceRewardType(amt, route, label);
    }

    @NotNull
    @Override
    public BoostedExperienceRewardType withAmountMultiplier(double multiplier) {
        return new BoostedExperienceRewardType(Math.max(1, (int) (amount * multiplier)), localizationRoute, displayLabel);
    }

    @NotNull
    @Override
    public OptionalLong getNumericAmount() {
        return OptionalLong.of(amount);
    }

    @NotNull
    @Override
    public BoostedExperienceRewardType withLocalizationRoute(@NotNull Route route) {
        return new BoostedExperienceRewardType(amount, route, displayLabel);
    }

    @NotNull
    @Override
    public BoostedExperienceRewardType withInlineDisplayLabel(@NotNull String label) {
        return new BoostedExperienceRewardType(amount, localizationRoute, label);
    }

    @NotNull
    @Override
    public String describeForDisplay() {
        return amount + " Boosted XP";
    }

    @NotNull
    @Override
    public String describeForDisplay(@NotNull McRPGPlayer player) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        Map<String, String> vars = Map.of("amount", String.valueOf(amount));

        if (localizationRoute != null) {
            try {
                String label = localization.getLocalizedMessage(player, localizationRoute);
                return prependDefaultColor(localization, player, label);
            } catch (Exception e) {
                McRPG.getInstance().getLogger().log(Level.WARNING,
                        "Failed to resolve display label for boosted_experience reward", e);
            }
        }
        if (!displayLabel.isEmpty()) {
            String label = localization.getLocalizedMessage(displayLabel, vars);
            return prependDefaultColor(localization, player, label);
        }
        try {
            String label = localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_BOOSTED_EXPERIENCE_FORMAT, vars);
            return prependDefaultColor(localization, player, label);
        } catch (Exception e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "Failed to resolve display label for boosted_experience reward", e);
            return describeForDisplay();
        }
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    /**
     * Deserializes the {@code amount} field from a pending-reward config map.
     *
     * @param config the serialized reward configuration
     * @return the parsed amount, or {@code 0} if missing or invalid
     */
    private static int deserializeAmount(@NotNull Map<String, Object> config) {
        if (!config.containsKey("amount")) {
            return 0;
        }
        Object raw = config.get("amount");
        if (raw instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(raw.toString());
        } catch (NumberFormatException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "Reward type '" + KEY + "' has non-numeric amount: " + raw, e);
            return 0;
        }
    }
}
