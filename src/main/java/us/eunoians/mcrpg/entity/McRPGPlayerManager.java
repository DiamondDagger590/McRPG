package us.eunoians.mcrpg.entity;

import com.diamonddagger590.mccore.player.PlayerManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * The implementation of a {@link PlayerManager} for McRPG. This provides
 * wrapping around McRPG implementations of {@link com.diamonddagger590.mccore.CorePlugin}
 * and {@link com.diamonddagger590.mccore.player.CorePlayer} so consumers don't need to cast
 * to the implementation they know is actually being used.
 */
public class McRPGPlayerManager extends PlayerManager<McRPG, McRPGPlayer> {

    public McRPGPlayerManager(@NotNull McRPG corePlugin) {
        super(corePlugin);
    }
}
