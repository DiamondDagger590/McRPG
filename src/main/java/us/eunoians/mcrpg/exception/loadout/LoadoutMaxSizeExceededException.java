package us.eunoians.mcrpg.exception.loadout;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.loadout.Loadout;

/**
 * This exception is thrown whenever an {@link us.eunoians.mcrpg.ability.Ability} is attempted to be added to a {@link Loadout}
 * but the loadout is already at its maximum size.
 */
public class LoadoutMaxSizeExceededException extends RuntimeException {

    private final Loadout loadout;

    public LoadoutMaxSizeExceededException(@NotNull Loadout loadout) {
        this.loadout = loadout;
    }

    public LoadoutMaxSizeExceededException(@NotNull Loadout loadout, @NotNull String message) {
        super(message);
        this.loadout = loadout;
    }

    /**
     * The {@link Loadout} that was attempted to be modified.
     *
     * @return The {@link Loadout} that was attempted to be modified.
     */
    @NotNull
    public Loadout getLoadout() {
        return loadout;
    }
}
