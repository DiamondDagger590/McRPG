package us.eunoians.mcrpg.exception.database;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;

/**
 * This exception is thrown whenever an {@link Ability} returns an empty {@link java.util.Optional}
 * for both {@link Ability#getDatabaseName()} and {@link Ability#getLegacyName()}.
 * <p>
 * An ability must provide one of them in order to be saved in the databases.
 */
public class AbilityDatabaseNameException extends RuntimeException {

    private final Ability ability;

    public AbilityDatabaseNameException(@NotNull Ability ability) {
        assert ability.getDatabaseName().isEmpty() && ability.getLegacyName().isEmpty();
        this.ability = ability;
    }

    /**
     * Gets the {@link Ability} that caused this exception
     *
     * @return The {@link Ability} that caused this exception.
     */
    @NotNull
    public Ability getAbility() {
        return ability;
    }

    @Override
    public String getMessage() {
        return "Ability with key " + ability.getAbilityKey().getKey() + " was missing both a database name and a legacy name. Please inform the developer as abilities must have one or the other";
    }
}
