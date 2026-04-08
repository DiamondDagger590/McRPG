package us.eunoians.mcrpg.skill.impl;

import com.diamonddagger590.mccore.statistic.SimpleStatistic;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.skill.BaseSkill;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A type of skill that is used by native McRPG skills.
 */
public abstract class McRPGSkill extends BaseSkill {

    public McRPGSkill(@NotNull NamespacedKey skillKey) {
        super(skillKey);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Native McRPG skills provide per-skill experience and max level statistics by default.
     * Concrete skills can override to add additional custom statistics alongside the defaults.
     * <p>
     * Display names are derived from the skill's {@link NamespacedKey} so this method is safe
     * to call during bootstrap before the localization manager is registered.
     */
    @Override
    @NotNull
    public Set<Statistic> getDefaultStatistics() {
        String displayName = titleCase(getSkillKey().getKey());
        return Set.of(
                new SimpleStatistic(getExperienceStatisticKey(), StatisticType.LONG, 0L,
                        displayName + " Experience", "Total " + displayName + " XP earned"),
                new SimpleStatistic(getMaxLevelStatisticKey(), StatisticType.INT, 0,
                        displayName + " Max Level", "Highest " + displayName + " level reached")
        );
    }

    /**
     * Converts a snake_case {@link NamespacedKey} key segment into a title-cased display string
     * without calling {@link #getName()}, making it safe to use before localization is loaded.
     * <p>
     * Example: {@code "mass_harvest"} → {@code "Mass Harvest"}.
     *
     * @param key The raw key segment (e.g. {@code "swords"}, {@code "mass_harvest"}).
     * @return A human-readable, title-cased string.
     */
    private static String titleCase(@NotNull String key) {
        return Arrays.stream(key.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
