package us.eunoians.mcrpg.ability.impl.type;

import com.diamonddagger590.mccore.statistic.SimpleStatistic;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An interface that represents an {@link Ability} that requires some sort of
 * action by an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} in order to activate.
 */
public interface ActiveAbility extends Ability {

    @Override
    default boolean isPassive() {
        return false;
    }

    /**
     * Gets the {@link NamespacedKey} for the activation count {@link Statistic} tracked for this ability.
     * <p>
     * The key follows the convention {@code <namespace>:<ability_key>_activations}, e.g.
     * {@code mcrpg:bleed_activations}.
     *
     * @return The {@link NamespacedKey} for this ability's activation count statistic.
     */
    @NotNull
    default NamespacedKey getActivationStatisticKey() {
        return new NamespacedKey(getPlugin(), getAbilityKey().getKey() + "_activations");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Active abilities provide a default activation count statistic. Concrete abilities
     * can override this to add additional custom statistics alongside the default.
     * <p>
     * Display names are derived from the ability's {@link NamespacedKey} so this method is
     * safe to call during bootstrap before the localization manager is registered.
     */
    @Override
    @NotNull
    default Set<Statistic> getDefaultStatistics() {
        String displayName = titleCase(getAbilityKey().getKey());
        return Set.of(new SimpleStatistic(
                getActivationStatisticKey(),
                StatisticType.LONG,
                0L,
                displayName + " Activations",
                "Times " + displayName + " has been activated"
        ));
    }

    /**
     * Converts a snake_case {@link NamespacedKey} key segment into a title-cased display string
     * without calling {@link #getName()}, making it safe to use before localization is loaded.
     * <p>
     * Example: {@code "mass_harvest"} → {@code "Mass Harvest"}.
     *
     * @param key The raw key segment (e.g. {@code "bleed"}, {@code "mass_harvest"}).
     * @return A human-readable, title-cased string.
     */
    private static String titleCase(@NotNull String key) {
        return Arrays.stream(key.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
