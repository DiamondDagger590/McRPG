package us.eunoians.mcrpg.exception.loadout;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.loadout.Loadout;

/**
 * This exception is thrown whenever an {@link us.eunoians.mcrpg.ability.Ability} is attempted to be added to a {@link Loadout}
 * but is not valid for that loadout.
 */
public class InvalidAbilityForLoadoutException extends RuntimeException {

    private final Loadout loadout;
    private final NamespacedKey abilityKey;

    public InvalidAbilityForLoadoutException(@NotNull Loadout loadout, @NotNull NamespacedKey abilityKey) {
        this.loadout = loadout;
        this.abilityKey = abilityKey;
    }
    public InvalidAbilityForLoadoutException(@NotNull Loadout loadout, @NotNull NamespacedKey abilityKey, @NotNull String message) {
        super(message);
        this.loadout = loadout;
        this.abilityKey = abilityKey;
    }

    /**
     * Retrieves the loadout associated with this exception.
     *
     * @return the loadout related to the exception, never null.
     */
    @NotNull
    public Loadout getLoadout() {
        return loadout;
    }

    /**
     * Retrieves the ability's {@link NamespacedKey} associated with this exception.
     *
     * @return The {@link NamespacedKey} of the ability related to this exception, never null.
     */
    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }
}
