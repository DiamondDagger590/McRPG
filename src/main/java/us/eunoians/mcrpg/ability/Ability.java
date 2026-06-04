package us.eunoians.mcrpg.ability;

import com.diamonddagger590.mccore.statistic.Statistic;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
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
     * Gets the localized name of this ability with its type color tag already resolved to a
     * MiniMessage-compatible string. For example, a passive ability's locale {@code name:} field
     * {@code <ability-passive><ability>} resolves to something like
     * {@code <color:#7FB87F>Enhanced Bleed} after palette replacement.
     * <p>
     * This sits between {@link #getName(McRPGPlayer)} (plain text, no color) and
     * {@link #getDisplayName(McRPGPlayer)} (fully parsed {@link Component}). Use this when
     * embedding an ability name as a MiniMessage placeholder value where the caller needs the
     * type color but does not yet want a parsed {@link Component}.
     * <p>
     * The default implementation returns {@link #getName(McRPGPlayer)} (no color).
     * {@link us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableAbility} overrides
     * this to resolve the locale {@code name:} field with palette substitution.
     *
     * @param player The player whose localization and palette to use.
     * @return A palette-resolved MiniMessage string containing the ability name with its type color.
     */
    @NotNull
    default String getColoredName(@NotNull McRPGPlayer player) {
        return getName(player);
    }

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
     * This method should not be used to determine if an ability activates or not. Instead, ensure that
     * {@link us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent}s are used.
     *
     * @param abilityHolder The {@link AbilityHolder} that is activating the ability.
     * @param event         The {@link Event} that triggered this ability.
     * @return {@code true} if the activation completed (or was never cancellable),
     *         {@code false} if the ability's internal Bukkit event was cancelled — the caller
     *         should refund mana when this returns {@code false}.
     */
    boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event);

    /**
     * Checks to see if this ability is enabled.
     *
     * @return {@code true} if this ability is enabled.
     */
    boolean isAbilityEnabled();

    /**
     * Gets the {@link AbilityType} classification of this ability.
     * <p>
     * Classification priority:
     * <ol>
     *   <li>{@link AbilityType#ACTIVE} — this ability implements {@link ComboActivatable}</li>
     *   <li>{@link AbilityType#PASSIVE} — this ability implements both {@link PassiveAbility}
     *       and {@link UnlockableAbility} (fires on events and has an unlock gate)</li>
     *   <li>{@link AbilityType#INNATE} — everything else, including always-on passives
     *       that implement {@link PassiveAbility} without an unlock gate</li>
     * </ol>
     * Individual ability classes may override this if the interface-based default does not
     * accurately represent their classification.
     *
     * @return the {@link AbilityType} of this ability
     */
    @NotNull
    default AbilityType getAbilityType() {
        if (this instanceof ComboActivatable) {
            return AbilityType.ACTIVE;
        }
        if (this instanceof PassiveAbility && this instanceof UnlockableAbility) {
            return AbilityType.PASSIVE;
        }
        return AbilityType.INNATE;
    }

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

    /**
     * Returns whether this ability is always available to its holder without requiring an
     * explicit unlock (e.g., innate abilities granted at skill level 0). An ability is
     * considered always-available when it does not carry the unlock attribute.
     * <p>
     * Distinct from {@link AbilityType} which classifies by activation pattern, and from
     * {@link us.eunoians.mcrpg.ability.impl.type.TierableAbility TierableAbility} which
     * controls tier progression.
     *
     * @return {@code true} if this ability does not require unlocking
     */
    default boolean isAlwaysAvailable() {
        return !(this instanceof UnlockableAbility);
    }

    /**
     * Gets the default {@link Statistic} definitions that should be tracked for this ability.
     * <p>
     * Subinterfaces like {@link us.eunoians.mcrpg.ability.impl.type.ActiveAbility} override this
     * to provide sensible defaults (e.g., an activation count statistic). Concrete ability
     * implementations can further override to add custom statistics specific to their mechanics.
     * <p>
     * <b>Important:</b> This method is a convenience helper — the returned statistics are
     * <b>not</b> automatically registered. They must be explicitly included in a
     * {@link us.eunoians.mcrpg.expansion.content.StatisticContentPack} by the owning
     * {@link us.eunoians.mcrpg.expansion.ContentExpansion} to be registered.
     *
     * @return A {@link Set} of default {@link Statistic} definitions for this ability.
     */
    @NotNull
    default Set<Statistic> getDefaultStatistics() {
        return Set.of();
    }
}
