package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Shared filter logic for ability-based quest objective types.
 * <p>
 * Encapsulates the common "specific ability key" / "ability type" / "any ability" filter
 * priority pattern used by {@link AbilityActivateObjectiveType}, {@link AbilityUnlockObjectiveType},
 * and {@link LoadoutEquipObjectiveType}.
 * <p>
 * Filter priority (first match wins):
 * <ol>
 *   <li>Specific ability key — matches only that ability</li>
 *   <li>Ability type ({@link AbilityType}) — matches abilities of that classification</li>
 *   <li>No filter — matches any ability</li>
 * </ol>
 */
public final class AbilityObjectiveFilter {

    /** A filter that matches all abilities. Used by unconfigured registry-registration instances. */
    public static final AbilityObjectiveFilter EMPTY = new AbilityObjectiveFilter(null, null);

    /** A sentinel filter that never matches any ability. Used when config parsing fails. */
    public static final AbilityObjectiveFilter NEVER_MATCH =
            new AbilityObjectiveFilter(new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown"), null);

    @Nullable
    private final NamespacedKey abilityFilter;
    @Nullable
    private final AbilityType abilityTypeFilter;

    /**
     * Creates a new ability objective filter with the given constraints.
     *
     * @param abilityFilter     specific ability key to match, or {@code null} for no key filter
     * @param abilityTypeFilter ability type classification, or {@code null} for no type filter
     */
    public AbilityObjectiveFilter(@Nullable NamespacedKey abilityFilter,
                                  @Nullable AbilityType abilityTypeFilter) {
        this.abilityFilter = abilityFilter;
        this.abilityTypeFilter = abilityTypeFilter;
    }

    /**
     * Returns {@code true} if the given ability satisfies the configured filters.
     * <p>
     * Priority order: specific key check first, then type check, then pass-all.
     *
     * @param ability the ability to evaluate
     * @return {@code true} if the ability passes all active filters
     */
    public boolean matchesAbility(@NotNull Ability ability) {
        if (abilityFilter != null) {
            return abilityFilter.equals(ability.getAbilityKey());
        }
        if (abilityTypeFilter != null) {
            return ability.getAbilityType() == abilityTypeFilter;
        }
        return true;
    }

    /**
     * Returns {@code true} if the filter is configured for a specific ability key and the given key matches it.
     * When no specific key filter is set, returns {@code true} (pass-all for key matching).
     *
     * @param key the ability key to check
     * @return {@code true} if the key matches or no key filter is set
     */
    public boolean matchesAbilityKey(@NotNull NamespacedKey key) {
        if (abilityFilter == null) {
            return true;
        }
        return abilityFilter.equals(key);
    }

    /**
     * Gets the specific ability key filter, if configured.
     *
     * @return the ability key filter, or empty if no key filter is set
     */
    @NotNull
    public Optional<NamespacedKey> getAbilityFilter() {
        return Optional.ofNullable(abilityFilter);
    }

    /**
     * Gets the ability type filter, if configured.
     *
     * @return the ability type filter, or empty if no type filter is set
     */
    @NotNull
    public Optional<AbilityType> getAbilityTypeFilter() {
        return Optional.ofNullable(abilityTypeFilter);
    }

    /**
     * Resolves the display name for the filtered ability by looking up the ability in the registry
     * and delegating to {@link Ability#getName()}. Falls back to the raw key if the ability is
     * not registered.
     *
     * @param abilityKey the ability key to resolve
     * @return the ability's display name from the registry, or the raw key as a fallback
     */
    @NotNull
    public String resolveAbilityName(@NotNull NamespacedKey abilityKey) {
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY);
        if (!abilityRegistry.registered(abilityKey)) {
            return abilityKey.getKey();
        }
        return abilityRegistry.getRegisteredAbility(abilityKey).getName();
    }
}
