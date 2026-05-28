package us.eunoians.mcrpg.ability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Classifies an ability into one of three mutually exclusive types based on its interface hierarchy.
 * <p>
 * Classification priority:
 * <ol>
 *   <li>{@link #ACTIVE} — implements {@link us.eunoians.mcrpg.ability.combo.ComboActivatable}</li>
 *   <li>{@link #PASSIVE} — implements both {@link us.eunoians.mcrpg.ability.impl.type.PassiveAbility}
 *       and {@link us.eunoians.mcrpg.ability.impl.type.UnlockableAbility} (has an unlock gate)</li>
 *   <li>{@link #INNATE} — everything else, including always-on passives that are not unlockable</li>
 * </ol>
 * The canonical classification of any ability instance is obtained via {@link Ability#getAbilityType()}.
 */
public enum AbilityType {

    /** Active ability triggered by a click combo. Implements {@link us.eunoians.mcrpg.ability.combo.ComboActivatable}. */
    ACTIVE,

    /**
     * Unlockable passive ability that fires automatically on events. Implements both
     * {@link us.eunoians.mcrpg.ability.impl.type.PassiveAbility} and
     * {@link us.eunoians.mcrpg.ability.impl.type.UnlockableAbility}.
     */
    PASSIVE,

    /**
     * Always-on innate ability — neither combo-activated nor gated behind an unlock.
     * Includes passives that implement {@link us.eunoians.mcrpg.ability.impl.type.PassiveAbility}
     * without an unlock gate.
     */
    INNATE;

    /**
     * Parses an {@link AbilityType} from a string value, case-insensitively.
     *
     * @param value the string to parse (e.g. {@code "ACTIVE"}, {@code "passive"})
     * @return an {@link Optional} containing the matching type, or empty if {@code value} is
     *         {@code null}, blank, or does not match any constant
     */
    @NotNull
    public static Optional<AbilityType> fromString(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
