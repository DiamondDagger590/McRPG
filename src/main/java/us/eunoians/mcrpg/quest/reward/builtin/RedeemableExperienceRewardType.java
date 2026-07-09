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
 * Built-in reward type that grants redeemable experience to a player's
 * {@link us.eunoians.mcrpg.entity.player.PlayerExperienceExtras} pool.
 * Redeemable experience can be allocated to specific skills by the player.
 * <p>
 * Config format:
 * <pre>
 * type: mcrpg:redeemable_experience
 * amount: 500
 * </pre>
 */
public class RedeemableExperienceRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "redeemable_experience");

    private final int amount;
    @Nullable
    private final Route localizationRoute;
    @NotNull
    private final String displayLabel;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public RedeemableExperienceRewardType() {
        this.amount = 0;
        this.localizationRoute = null;
        this.displayLabel = "";
    }

    private RedeemableExperienceRewardType(int amount, @Nullable Route localizationRoute, @NotNull String displayLabel) {
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
    public RedeemableExperienceRewardType parseConfig(@NotNull Section section) {
        int amount = section.getInt("amount", 0);
        if (amount <= 0) {
            McRPG.getInstance().getLogger().warning(
                    "Reward type '" + KEY + "' configured with amount <= 0 (" + amount + ") — reward will have no effect");
        }
        return new RedeemableExperienceRewardType(amount, null, "");
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
        mcRPGPlayerOpt.ifPresent(p -> p.getExperienceExtras().modifyRedeemableExperience(amount));
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
    public RedeemableExperienceRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        int amt = deserializeAmount(config);
        if (amt <= 0) {
            McRPG.getInstance().getLogger().warning(
                    "Reward type '" + KEY + "' deserialized with amount <= 0 (" + amt + ") — reward will have no effect");
        }
        Route route = config.containsKey("localization-route")
                ? Route.fromString(config.get("localization-route").toString())
                : null;
        String label = config.getOrDefault("display", "").toString();
        return new RedeemableExperienceRewardType(amt, route, label);
    }

    @Override
    public boolean isScalable() {
        return true;
    }

    @NotNull
    @Override
    public RedeemableExperienceRewardType withAmountMultiplier(double multiplier) {
        return new RedeemableExperienceRewardType(Math.max(1, (int) (amount * multiplier)), localizationRoute, displayLabel);
    }

    @NotNull
    @Override
    public RedeemableExperienceRewardType withExactAmount(long exactAmount) {
        return new RedeemableExperienceRewardType((int) exactAmount, localizationRoute, displayLabel);
    }

    @NotNull
    @Override
    public OptionalLong getNumericAmount() {
        return OptionalLong.of(amount);
    }

    @NotNull
    @Override
    public RedeemableExperienceRewardType withLocalizationRoute(@NotNull Route route) {
        return new RedeemableExperienceRewardType(amount, route, displayLabel);
    }

    @NotNull
    @Override
    public RedeemableExperienceRewardType withInlineDisplayLabel(@NotNull String label) {
        return new RedeemableExperienceRewardType(amount, localizationRoute, label);
    }

    @NotNull
    @Override
    public String describeForDisplay() {
        return amount + " Redeemable XP";
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
                        "Failed to resolve display label for redeemable_experience reward", e);
            }
        }
        if (!displayLabel.isEmpty()) {
            String label = localization.getLocalizedMessage(displayLabel, vars);
            return prependDefaultColor(localization, player, label);
        }
        try {
            String label = localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_REDEEMABLE_EXPERIENCE_FORMAT, vars);
            return prependDefaultColor(localization, player, label);
        } catch (Exception e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "Failed to resolve display label for redeemable_experience reward", e);
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
