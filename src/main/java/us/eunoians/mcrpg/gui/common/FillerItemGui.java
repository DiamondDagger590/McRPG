package us.eunoians.mcrpg.gui.common;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.common.McRPGFillerSlot;

/**
 * A helper class for any McRPG gui that uses a filler item slot to indicate
 * a blank slot that can't be filled. An example would be unused spaces in a navigation
 * bar.
 */
public interface FillerItemGui extends Gui<McRPGPlayer> {

    /**
     * Gets a {@link Slot} to be used as the "filler" item for a gui
     * that wants a blank slot which can't be filled and indicates it
     * has zero functionality.
     *
     * @return The filler {@link Slot}.
     */
    @NotNull
    default Slot<McRPGPlayer> getFillerItemSlot() {
        return new McRPGFillerSlot();
    }
}
