package us.eunoians.mcrpg.entity;

import com.diamonddagger590.mccore.player.PlayerManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

public class McRPGPlayerManager extends PlayerManager<McRPG, McRPGPlayer> {

    public McRPGPlayerManager(@NotNull McRPG corePlugin) {
        super(corePlugin);
    }
}
