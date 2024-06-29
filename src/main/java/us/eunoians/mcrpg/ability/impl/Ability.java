package us.eunoians.mcrpg.ability.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Optional;
import java.util.Set;

/**
 * The base interface for all abilities, providing basic behavior outlines that gain some definition
 * in {@link BaseAbility}.
 * <p>
 * Further ability behavior is provided in child interfaces which can be implemented alongside
 * extending {@link BaseAbility} in order to provide more out-of-the-box behavior.
 */
public interface Ability {

    /**
     * Gets the {@link NamespacedKey} of this ability.
     *
     * @return The {@link NamespacedKey} of this ability.
     */
    @NotNull
    NamespacedKey getAbilityKey();

    /**
     * Gets a {@link Set} of all {@link us.eunoians.mcrpg.ability.attribute.AbilityAttribute AbilityAttributes} that
     * this ability utilizes.
     *
     * @return A {@link Set} of all {@link us.eunoians.mcrpg.ability.attribute.AbilityAttribute AbilityAttributes} that
     * this ability utilizes.
     */
    @NotNull
    Set<NamespacedKey> getApplicableAttributes();

    /**
     * Checks to see if this ability belongs to a {@link us.eunoians.mcrpg.skill.Skill}
     *
     * @return {@code true} if the ability belongs to a {@link us.eunoians.mcrpg.skill.Skill}
     */
    default boolean belongsToSkill() {
        return getSkill().isPresent();
    }

    /**
     * Gets an {@link Optional} that will be empty or contain the {@link NamespacedKey} of the
     * {@link us.eunoians.mcrpg.skill.Skill} this ability belongs to.
     *
     * @return An {@link Optional} that will be empty or contain the {@link NamespacedKey} of the
     * {@link us.eunoians.mcrpg.skill.Skill} this ability belongs to.
     */
    @NotNull
    Optional<NamespacedKey> getSkill();

    /**
     * Gets an {@link Optional} that will be empty or contain the legacy name of this ability.
     * <p>
     * This is only used for abilities that existed before the recode in order to support
     * legacy database table conversions.
     *
     * @return An {@link Optional} that will be empty or contain the legacy name of this ability.
     */
    @NotNull
    default Optional<String> getLegacyName() {
        return Optional.empty();
    }

    /**
     * Gets an {@link Optional} containing the database name for an ability. This is an internal
     * use only name that is used for database storage.
     * <p>
     * The {@link Optional} will be empty if this is a legacy ability since there is code to convert
     * {@link #getLegacyName()} to its old form for this use.
     *
     * @return An {@link Optional} containing the database name for an ability. This is an internal
     * use only name that is used for database storage.
     */
    @NotNull
    Optional<String> getDatabaseName();

    /**
     * Gets the name to display in messages or guis for this ability.
     *
     * @return The name to display in messages or guis for this ability.
     */
    @NotNull
    String getDisplayName();

    /**
     * Gets the {@link ItemStack} to use when displaying this ability in guis.
     *
     * @param abilityHolder The {@link AbilityHolder} that is needing an item displayed for.
     * @return The {@link ItemStack} to use when displaying this ability in guis.
     */
    @NotNull
    ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder);

    /**
     * Activates this ability for the given {@link AbilityHolder} with the provided {@link Event} being the trigger.
     * <p>
     * This method should not be used to determine if an ability activates or not. Instead, ensure that {@link us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent}s are used.
     *
     * @param abilityHolder The {@link AbilityHolder} that is activating the ability
     * @param event         The {@link Event} that triggered this ability.
     */
    void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event);

    /**
     * Checks to see if this ability is enabled.
     *
     * @return {@code true} if this ability is enabled.
     */
    boolean isAbilityEnabled();

    /**
     * Checks to see if this ability is passive or is active (requires user action to activate).
     *
     * @return {@code true} if this ability is passive.
     */
    boolean isActivePassive();

    /**
     * Gets the {@link ReadyData} that is used whenever this ability enters a "ready" state for
     * an {@link AbilityHolder}.
     * <p>
     * This should return an empty {@link Optional} whenever this ability doesn't actually ready.
     *
     * @return An {@link Optional} containing the {@link ReadyData} that is used whenever this ability
     * enters a "ready" state, or an empty {@link Optional} if this ability isn't one that supports the ready
     * mechanic.
     */
    @NotNull
    Optional<ReadyData> getReadyData();
}
