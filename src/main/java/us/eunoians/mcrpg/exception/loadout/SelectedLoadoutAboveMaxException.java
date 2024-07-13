package us.eunoians.mcrpg.exception.loadout;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;

public class SelectedLoadoutAboveMaxException extends RuntimeException {

    private final LoadoutHolder loadoutHolder;
    private final int loadoutSlot;

    public  SelectedLoadoutAboveMaxException(@NotNull LoadoutHolder loadoutHolder, int loadoutSlot) {
        this.loadoutHolder = loadoutHolder;
        this.loadoutSlot = loadoutSlot;
    }

    @NotNull
    public LoadoutHolder getLoadoutHolder() {
        return loadoutHolder;
    }

    public int getLoadoutSlot() {
        return loadoutSlot;
    }
}
