package us.eunoians.mcrpg.exception.ready;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * An exception that gets thrown whenever an {@link AbilityHolder} tries
 * to ready using an {@link Ability} that doesn't support the ready status.
 */
public class AbilityNotValidToReadyException extends RuntimeException {

    private final AbilityHolder abilityHolder;
    private final Ability ability;

    public AbilityNotValidToReadyException(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability) {
        this.abilityHolder = abilityHolder;
        this.ability = ability;
    }

    public AbilityNotValidToReadyException(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability, @NotNull String message) {
        super(message);
        this.abilityHolder = abilityHolder;
        this.ability = ability;
    }

    /**
     * Gets the {@link AbilityHolder} that failed to be readied.
     *
     * @return The {@link AbilityHolder} that failed to be readied.
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    /**
     * Gets the {@link Ability} that failed to be readied.
     *
     * @return The {@link Ability} that failed to be readied.
     */
    @NotNull
    public Ability getAbility() {
        return ability;
    }
}
