package us.eunoians.mcrpg.ability;

import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This class stores all information about {@link Ability Abilities}. Instances of this
 * class that are associated with entities can be found by using {@link us.eunoians.mcrpg.entity.holder.AbilityHolder}.
 * <p>
 * This class allows for the storing of information in an abstract manner about any given {@link Ability}
 * through using {@link AbilityAttribute AbilityAttributes}. An example of this would be an ability's tier
 * through the {@link us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager#ABILITY_TIER_ATTRIBUTE_KEY}.
 * <p>
 * In the previous iteration of this plugin, every ability could have unique behavior which would require custom handling.
 * This is highlighted by ability tiers, where abilities that a player unlocked would have tiers whilst abilities that
 * come inate with a skill don't have tiers. This new system allows for this information to be stored abstractly at a high level
 * and abilities to be treated in an agnostic manner without needing to know their specific features and constraints.
 */
public class AbilityData {

    private final NamespacedKey abilityKey;
    private final Map<NamespacedKey, AbilityAttribute<?>> abilityAttributes;

    public AbilityData(@NotNull NamespacedKey abilityKey, @NotNull AbilityAttribute<?>... abilityAttributes) {
        this.abilityKey = abilityKey;
        this.abilityAttributes = new HashMap<>();

        for (AbilityAttribute<?> abilityAttribute : abilityAttributes) {
            addAttribute(abilityAttribute);
        }
    }

    public AbilityData(@NotNull NamespacedKey abilityKey, @NotNull Collection<AbilityAttribute<?>> abilityAttributes) {
        this.abilityKey = abilityKey;
        this.abilityAttributes = new HashMap<>();

        for (AbilityAttribute<?> abilityAttribute : abilityAttributes) {
            addAttribute(abilityAttribute);
        }
    }

    /**
     * Gets the {@link NamespacedKey} representation of the {@link Ability} that this {@link AbilityData}
     * belongs to.
     *
     * @return The {@link NamespacedKey} representation of the {@link Ability} that this {@link AbilityData}
     * belongs to.
     */
    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    /**
     * Checks to see if the ability represented by this data has the corresponding {@link AbilityAttribute}.
     *
     * @param abilityAttribute The {@link AbilityAttribute} to check
     * @return {@code true} if the ability represented by this data has the corresponding {@link AbilityAttribute}
     */
    public boolean doesAbilityHaveAttribute(@NotNull AbilityAttribute<?> abilityAttribute) {
        return doesAbilityHaveAttribute(abilityAttribute.getNamespacedKey());
    }

    /**
     * Checks to see if the ability represented by this data has the corresponding {@link AbilityAttribute} that
     * is mapped to the provided {@link NamespacedKey}.
     *
     * @param namespacedKey The {@link NamespacedKey} to check
     * @return {@code true} if the ability represented by this data has the corresponding {@link AbilityAttribute}
     * that is mapped to the provided {@link NamespacedKey}
     */
    public boolean doesAbilityHaveAttribute(@NotNull NamespacedKey namespacedKey) {
        return abilityAttributes.containsKey(namespacedKey);
    }

    /**
     * Gets the {@link AbilityAttribute} that corresponds with the provided {@link NamespacedKey}
     * from the {@link Ability} that is represented by this data.
     *
     * @param namespacedKey The {@link NamespacedKey} of the {@link AbilityAttribute} to get
     * @return An {@link Optional} that either be empty if no matches were found or
     * will contain the {@link AbilityAttribute} object that corresponds with the provided {@link NamespacedKey}.
     */
    @NotNull
    public Optional<AbilityAttribute<?>> getAbilityAttribute(@NotNull NamespacedKey namespacedKey) {
        return Optional.ofNullable(abilityAttributes.get(namespacedKey));
    }

    /**
     * Gets an {@link ImmutableSet} that contains all {@link NamespacedKey NamespacedKeys} that represent {@link AbilityAttribute AbilityAttributes} that
     * are stored inside of this data.
     *
     * @return An {@link ImmutableSet} that contains all {@link NamespacedKey NamespacedKeys} that represents {@link AbilityAttribute AbilityAttributes} that
     * are stored inside of this data.
     */
    @NotNull
    public Set<NamespacedKey> getAllAttributeKeys() {
        return ImmutableSet.copyOf(abilityAttributes.keySet());
    }

    /**
     * Gets an {@link ImmutableSet} that contains all {@link AbilityAttribute AbilityAttributes} that are stored
     * inside of this data.
     *
     * @return An {@link ImmutableSet} that contains all {@link AbilityAttribute AbilityAttributes} that are stored inside of this data.
     */
    public Set<AbilityAttribute<?>> getAllAttributes() {
        return ImmutableSet.copyOf(abilityAttributes.values());
    }

    /**
     * Checks to see if this data has the provided {@link NamespacedKey} as an {@link AbilityAttribute} inside
     * of this data.
     *
     * @param attributeKey The {@link NamespacedKey} to check
     * @return {@code true} if the provided {@link NamespacedKey} is stored as an {@link AbilityAttribute} inside
     * of this data.
     */
    public boolean hasAttribute(@NotNull NamespacedKey attributeKey) {
        return abilityAttributes.containsKey(attributeKey);
    }

    /**
     * Adds the provided {@link AbilityAttribute} to this data.
     *
     * @param abilityAttribute The {@link AbilityAttribute} to add
     */
    public void addAttribute(@NotNull AbilityAttribute<?> abilityAttribute) {
        abilityAttributes.put(abilityAttribute.getNamespacedKey(), abilityAttribute);
    }

    /**
     * Removes the provided {@link AbilityAttribute} from this data.
     *
     * @param abilityAttribute The {@link AbilityAttribute} to remove
     */
    public void removeAttribute(@NotNull AbilityAttribute<?> abilityAttribute) {
        removeAttribute(abilityAttribute.getNamespacedKey());
    }

    /**
     * Removes the {@link AbilityAttribute} associated with the provided {@link NamespacedKey}
     * from this data.
     *
     * @param namespacedKey The {@link NamespacedKey} to remove the associated {@link AbilityAttribute} for
     */
    public void removeAttribute(@NotNull NamespacedKey namespacedKey) {
        abilityAttributes.remove(namespacedKey);
    }

    /**
     * Replaces the existing ability attribute with a new one containing the provided content
     *
     * @param abilityAttribute The ability attribute to update
     * @param content          The content to store inside the ability attribute
     * @param <T>              The type of content stored inside the attribute
     */
    public <T> void updateAttribute(@NotNull AbilityAttribute<T> abilityAttribute, T content) {
        addAttribute(abilityAttribute.create(content));
    }
}
