package us.eunoians.mcrpg.util.filter.core;

import com.diamonddagger590.mccore.util.filter.ChainPlayerContextFilter;
import com.diamonddagger590.mccore.util.filter.PlayerContextFilter;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

public class McRPGChainPlayerContextFilter<E> extends ChainPlayerContextFilter<E, McRPGPlayer> {

    @SafeVarargs
    public McRPGChainPlayerContextFilter(@NotNull PlayerContextFilter<E, McRPGPlayer>... filters) {
        super(filters);
    }
}
