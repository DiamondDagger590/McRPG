package us.eunoians.mcrpg.gui;

import com.diamonddagger590.mccore.gui.GuiManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

public class McRPGGuiManager extends GuiManager<McRPGPlayer, McRPG> {

    public McRPGGuiManager(@NotNull McRPG plugin) {
        super(plugin);
    }
}
