package us.eunoians.mcrpg.util.filter.core;

import com.diamonddagger590.mccore.util.filter.PlayerContextFilter;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * A utility wrapper around {@link PlayerContextFilter} to provide {@link McRPGPlayer}
 * automatically.
 *
 * @param <E> The type of data being filtered.
 */
public interface McRPGPlayerContextFilter<E> extends PlayerContextFilter<E, McRPGPlayer> {
}
