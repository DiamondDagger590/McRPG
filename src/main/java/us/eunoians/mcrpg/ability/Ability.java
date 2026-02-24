package us.eunoians.mcrpg.ability;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Map;
import java.util.Set;

/**
 * The base interface for all abilities, providing basic behavior outlines that gain some definition
 * in {@link BaseAbility}.
 * <p>
 * Further ability behavior is provided in child interfaces which can be implemented alongside
 * extending {@link BaseAbility} in order to provide more out-of-the-box behavior.
 */
public interface Ability extends McRPGContent {

    /**
     * Gets the {@link Plugin} that owns this ability.
     *
     * @return The {@link Plugin} that owns this ability.
     */
    @NotNull
    Plugin getPlugin();

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
     * Gets the database name for an ability. This is an internal
     * use-only name used for database storage.
     *
     * @return The database name for an ability. This is an internal
     * use-only name used for database storage.
     */
    @NotNull
    String getDatabaseName();

    /**
     * Gets the localized name of the ability for the provided {@link McRPGPlayer}.
     *
     * @param player The player whose localization to use.
     * @return The localized name of the ability.
     */
    @NotNull
    String getName(@NotNull McRPGPlayer player);

    /**
     * Gets the localized name of the ability using the default locale.
     *
     * @return The localized name of the ability.
     */
    @NotNull
    String getName();

    /**
     * Gets the name to display in messages or guis for this ability. This may have a placeholder
     * such as {@code <ability-name>} which should be replaced by {@link #getName()}.
     *
     * @param player The {@link McRPGPlayer} to get the localized display name for.
     * @return The name to display in messages or guis for this ability.
     */
    @NotNull
    Component getDisplayName(@NotNull McRPGPlayer player);

    /**
     * Gets the name to display in messages or guis for this ability using the default locale.
     * This may have a placeholder such as {@code <ability-name>} which should be replaced by {@link #getName()}.
     *
     * @return The name to display in messages or guis for this ability.
     */
    @NotNull
    Component getDisplayName();

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
    boolean isPassive();

    /**
     * Gets the {@link AbilityItemBuilder} for this ability based off the provided
     * {@link McRPGPlayer}.
     *
     * @param player The {@link McRPGPlayer} to get an item builder for.
     * @return The {@link AbilityItemBuilder} for this ability based off the provided
     * {@link McRPGPlayer}.
     */
    @NotNull
    AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player);

    /**
     * Gets a map containing the placeholders supported for this ability using the given
     * {@link McRPGPlayer}.
     * <p>
     * The key will be the placeholder itself whilst the value will be the string to replace the
     * placeholder with. Placeholders should follow the format of {@code <example>}.
     * <p>
     * Some generic placeholders are provided out of box in the {@link AbilityItemBuilder}
     * itself,
     *
     * @param player The player to build the placeholders for.
     * @return A map containing the placeholders to use for this ability display.
     */
    @NotNull
    default Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        return Map.of();
    }
}
