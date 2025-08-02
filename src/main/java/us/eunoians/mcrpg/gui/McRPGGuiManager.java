package us.eunoians.mcrpg.gui;

import com.diamonddagger590.mccore.gui.GuiManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * The implementation of a {@link GuiManager} for McRPG. This provides
 * wrapping around McRPG implementations of {@link com.diamonddagger590.mccore.CorePlugin}
 * and {@link com.diamonddagger590.mccore.player.CorePlayer} so consumers don't need to cast
 * to the implementation they know is actually being used.
 */
public class McRPGGuiManager extends GuiManager<McRPGPlayer, McRPG> {

    public McRPGGuiManager(@NotNull McRPG plugin) {
        super(plugin);
    }
}
