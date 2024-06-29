package us.eunoians.mcrpg.exception.ready;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This exception is fired whenever an {@link AbilityHolder} is
 * already ready but tries to become ready again.
 */
public class HolderAlreadyReadyException extends RuntimeException {

    private final AbilityHolder abilityHolder;

    public HolderAlreadyReadyException(@NotNull AbilityHolder abilityHolder) {
        this.abilityHolder = abilityHolder;
    }

    public HolderAlreadyReadyException(@NotNull AbilityHolder abilityHolder, @NotNull String message) {
        super(message);
        this.abilityHolder = abilityHolder;
    }

    /**
     * Gets the {@link AbilityHolder} that tried to become ready.
     *
     * @return The {@link AbilityHolder} that tried to become ready.
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }
}
