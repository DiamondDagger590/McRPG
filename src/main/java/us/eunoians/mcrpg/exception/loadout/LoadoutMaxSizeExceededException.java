package us.eunoians.mcrpg.exception.loadout;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.loadout.Loadout;

public class LoadoutMaxSizeExceededException extends RuntimeException {

    private final Loadout loadout;

    public LoadoutMaxSizeExceededException(@NotNull Loadout loadout) {
        this.loadout = loadout;
    }

    public LoadoutMaxSizeExceededException(@NotNull Loadout loadout, @NotNull String message) {
        super(message);
        this.loadout = loadout;
    }

    @NotNull
    public Loadout getLoadout() {
        return loadout;
    }
}
