package us.eunoians.mcrpg.util.comparator;

import com.diamonddagger590.mccore.util.comparator.PlayerContextComparator;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * A utility wrapper around {@link PlayerContextComparator} to provide {@link McRPGPlayer}
 * automatically.
 *
 * @param <E> The type of data being compared.
 */
public interface McRPGPlayerContextComparator<E> extends PlayerContextComparator<E, McRPGPlayer> {
}
