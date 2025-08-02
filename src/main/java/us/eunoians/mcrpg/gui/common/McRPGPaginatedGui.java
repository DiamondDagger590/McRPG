package us.eunoians.mcrpg.gui.common;

import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.gui.slot.pagination.NextPageSlot;
import com.diamonddagger590.mccore.gui.slot.pagination.PreviousPageSlot;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.common.McRPGNextPageSlot;
import us.eunoians.mcrpg.gui.slot.common.McRPGPreviousPageSlot;

/**
 * A helper class for any McRPG gui that uses pagination by providing common
 * slots suck as filler slots and next/previous page slots.
 */
public abstract class McRPGPaginatedGui extends PaginatedGui<McRPGPlayer> implements FillerItemGui {

    public McRPGPaginatedGui(@NotNull McRPGPlayer corePlayer) {
        super(corePlayer);
    }

    public McRPGPaginatedGui(@NotNull McRPGPlayer corePlayer, int page) {
        super(corePlayer, page);
    }

    @NotNull
    @Override
    public PreviousPageSlot<McRPGPlayer> getPreviousPageSlot() {
        return new McRPGPreviousPageSlot();
    }

    @NotNull
    @Override
    public NextPageSlot<McRPGPlayer> getNextPageSlot() {
        return new McRPGNextPageSlot();
    }
}
