package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Built-in reward type that grants McRPG skill experience to a player.
 * <p>
 * Config format:
 * <pre>
 * type: mcrpg:experience
 * skill: MINING
 * amount: 500
 * </pre>
 * <p>
 * Display label resolution order:
 * <ol>
 *     <li>Auto-derived quest-scoped / template-scoped localization route</li>
 *     <li>Inline {@code display.rewards.<label>} from the quest's {@code display:} block,
 *         resolved through MiniMessage with {@code <amount>} and {@code <skill>} placeholders</li>
 *     <li>{@link LocalizationKey#QUEST_REWARD_EXPERIENCE_FORMAT} type-level format template
 *         with the same placeholders (configurable globally in {@code en_quest.yml})</li>
 * </ol>
 */
public class ExperienceRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "experience");

    private final String skillName;
    private final long amount;
    @Nullable
    private final Route localizationRoute;
    @NotNull
    private final String displayLabel;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public ExperienceRewardType() {
        this.skillName = "";
        this.amount = 0;
        this.localizationRoute = null;
        this.displayLabel = "";
    }

    private ExperienceRewardType(@NotNull String skillName, long amount,
                                 @Nullable Route localizationRoute, @NotNull String displayLabel) {
        this.skillName = skillName;
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
    public ExperienceRewardType parseConfig(@NotNull Section section) {
        return new ExperienceRewardType(
                section.getString("skill", ""),
                section.getLong("amount", 0L),
                null,
                "");
    }

    @NotNull
    @Override
    public ExperienceRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String skill = config.getOrDefault("skill", "").toString();
        long amt = config.containsKey("amount") ? ((Number) config.get("amount")).longValue() : 0;
        Route route = config.containsKey("localization-route")
                ? Route.fromString(config.get("localization-route").toString())
                : null;
        String label = config.getOrDefault("display", "").toString();
        return new ExperienceRewardType(skill, amt, route, label);
    }

    @Override
    public void grant(@NotNull Player player) {
        if (skillName.isEmpty() || amount <= 0) {
            return;
        }

        NamespacedKey skillKey = McRPGMethods.parseNamespacedKey(skillName);
        if (skillKey == null || !RegistryAccess.registryAccess().registry(McRPGRegistryKey.SKILL).registered(skillKey)) {
            McRPG.getInstance().getLogger().warning("Cannot grant experience — unknown skill: " + skillName);
            return;
        }

        Optional<McRPGPlayer> mcRPGPlayer = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());

        mcRPGPlayer.ifPresent(p ->
                p.asSkillHolder().getSkillHolderData(skillKey)
                        .ifPresent(data -> data.addExperience((int) amount)));
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("skill", skillName);
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
    public OptionalLong getNumericAmount() {
        return OptionalLong.of(amount);
    }

    @Override
    public boolean isScalable() {
        return true;
    }

    @NotNull
    @Override
    public ExperienceRewardType withAmountMultiplier(double multiplier) {
        long scaled = Math.max(1, (long) (amount * multiplier));
        return new ExperienceRewardType(skillName, scaled, localizationRoute, displayLabel);
    }

    @NotNull
    @Override
    public ExperienceRewardType withExactAmount(long exactAmount) {
        return new ExperienceRewardType(skillName, exactAmount, localizationRoute, displayLabel);
    }

    @NotNull
    @Override
    public ExperienceRewardType withLocalizationRoute(@NotNull Route route) {
        return new ExperienceRewardType(skillName, amount, route, displayLabel);
    }

    @NotNull
    @Override
    public ExperienceRewardType withInlineDisplayLabel(@NotNull String label) {
        return new ExperienceRewardType(skillName, amount, localizationRoute, label);
    }

    @NotNull
    @Override
    public String describeForDisplay() {
        String skill = formatSkillName(skillName);
        String format = "<amount> <skill> XP";
        return format.replace("<amount>", String.valueOf(amount)).replace("<skill>", skill);
    }

    @NotNull
    @Override
    public String describeForDisplay(@NotNull McRPGPlayer player) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        Map<String, String> vars = rewardVars();
        String label;

        if (localizationRoute != null) {
            try {
                label = localization.getLocalizedMessage(player, localizationRoute);
                return prependDefaultColor(localization, player, label);
            } catch (Exception ignored) {
                // Fall through to inline display label
            }
        }
        if (!displayLabel.isEmpty()) {
            label = localization.getLocalizedMessage(displayLabel, vars);
            return prependDefaultColor(localization, player, label);
        }
        try {
            label = localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_EXPERIENCE_FORMAT, vars);
            return prependDefaultColor(localization, player, label);
        } catch (Exception ignored) {
            return describeForDisplay();
        }
    }

    /**
     * Builds the placeholder variable map for MiniMessage resolution.
     * Keys match the placeholders documented in {@code en_quest.yml}:
     * {@code <amount>} and {@code <skill>}.
     */
    @NotNull
    private Map<String, String> rewardVars() {
        return Map.of(
                "amount", String.valueOf(amount),
                "skill", formatSkillName(skillName));
    }

    /**
     * Formats a raw skill name (e.g. {@code "SWORDS"} or {@code "mcrpg:mining"}) into a
     * human-readable display name (e.g. {@code "Swords"} or {@code "Mining"}).
     */
    @NotNull
    private String formatSkillName(@NotNull String raw) {
        if (raw.isEmpty()) {
            return "Unknown";
        }
        if (raw.contains(":")) {
            raw = raw.substring(raw.indexOf(':') + 1);
        }
        String[] parts = raw.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}

