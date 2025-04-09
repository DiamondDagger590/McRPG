package us.eunoians.mcrpg.gui;

import com.diamonddagger590.mccore.gui.PaginatedGui;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

public abstract class McRPGPaginatedGui extends PaginatedGui {

    public McRPGPaginatedGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
    }

    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        assert(getCreatingPlayer().isPresent());
        return (McRPGPlayer) getCreatingPlayer().get();
    }
}
