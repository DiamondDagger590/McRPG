package us.eunoians.mcrpg.ability.unlock;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

/**
 * A type of unlock condition that gates an {@link us.eunoians.mcrpg.ability.impl.type.UnlockableAbility}.
 * <p>
 * Mirrors the {@link us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType} pattern: a base
 * (unconfigured) instance is registered in the {@link UnlockConditionTypeRegistry};
 * {@link #parseConfig(Section)} is called once per config entry to produce a new immutable
 * configured instance of the same concrete type. Java-authored defaults skip {@code parseConfig}
 * and use a public constructor that accepts the same fields the YAML would set.
 * <p>
 * A configured instance answers three independent questions:
 * <ul>
 *   <li>{@link #isMet(AbilityHolder)} — is the holder currently eligible? Drives the login
 *       sweep and the skill level-up flow.</li>
 *   <li>{@link #getDisplayDescription(McRPGPlayer)} / {@link #getDisplayLabel(McRPGPlayer)} —
 *       how is the requirement rendered, including the player's current progress?</li>
 *   <li>{@link #getProgress(AbilityHolder)} — what fraction is satisfied, for progress bars?</li>
 * </ul>
 * <p>
 * Calling {@code isMet} on the unconfigured registry prototype is harmless: every built-in
 * checks for empty config fields and returns {@code false} ({@code getDisplayDescription}
 * renders a degraded but stable label). The prototype is never expected to be evaluated; this
 * defensive contract simply prevents accidents.
 * <p>
 * Extends {@link McRPGContent} so types are distributable through the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system — McRPG's built-ins are
 * registered through the native {@code McRPGExpansion} via a
 * {@link us.eunoians.mcrpg.expansion.content.UnlockConditionTypeContentPack}, the same path
 * third-party expansions use.
 */
public interface UnlockConditionType extends McRPGContent {

    /**
     * Unique key identifying this condition type (e.g. {@code mcrpg:skill_level}).
     *
     * @return the namespaced key
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Parses one condition's type-specific config into a new configured instance of this
     * concrete type. The section is the body under a single named condition entry — the
     * {@code type} key has already been consumed by the resolution manager and is guaranteed
     * to match {@link #getKey()}. Composite types ({@code mcrpg:all_of} / {@code mcrpg:any_of})
     * recurse via the {@link UnlockConditionManager}.
     *
     * @param section the config section for this entry
     * @return a configured instance of the same concrete type
     * @throws UnlockConditionParseException if the section is missing required keys or
     *         contains invalid values
     */
    @NotNull
    UnlockConditionType parseConfig(@NotNull Section section);

    /**
     * Inverse of {@link #parseConfig(Section)} — writes this configured instance back into a
     * config section. Default implementation throws; types that support admin-tool
     * serialization override it.
     *
     * @param section the destination section to populate
     */
    default void serializeConfig(@NotNull Section section) {
        throw new UnsupportedOperationException(
                "UnlockConditionType " + getKey() + " does not support serializeConfig");
    }

    /**
     * Whether the holder currently satisfies this configured condition. Must be pure — must
     * not mutate holder state or schedule side-effects. Never throws; failure modes (missing
     * skill, PAPI absent, called on the unconfigured registry prototype) return {@code false}.
     *
     * @param holder the holder to evaluate against
     * @return {@code true} if the holder meets the requirement
     */
    boolean isMet(@NotNull AbilityHolder holder);

    /**
     * Full localized description for lore / tooltip rendering, resolved through the player's
     * locale chain and interpolating the player's current progress via the {@code <current>}
     * placeholder where the type supports it.
     *
     * @param player the player whose locale chain and state drive rendering
     * @return the localized description component
     */
    @NotNull
    Component getDisplayDescription(@NotNull McRPGPlayer player);

    /**
     * Short label for compact rendering (sidebar entries, sort hints). Defaults to the
     * description.
     *
     * @param player the player whose locale chain and state drive rendering
     * @return the localized label component
     */
    @NotNull
    default Component getDisplayLabel(@NotNull McRPGPlayer player) {
        return getDisplayDescription(player);
    }

    /**
     * Progress toward the requirement in {@code [0.0, 1.0]}. Binary conditions return
     * {@code 0.0} until met, {@code 1.0} when met. Rendering-only.
     *
     * @param holder the holder to evaluate against
     * @return progress fraction
     */
    default double getProgress(@NotNull AbilityHolder holder) {
        return isMet(holder) ? 1.0 : 0.0;
    }

    /**
     * Whether this condition is purely informational — it can never be met by McRPG
     * (e.g. {@code mcrpg:display_hint}). Used by the empty-display startup warning and by
     * the GUI to suppress the progress bar on a hint.
     *
     * @return {@code true} if this condition is display-only
     */
    default boolean isDisplayOnly() {
        return false;
    }
}
