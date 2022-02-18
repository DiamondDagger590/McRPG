package us.eunoians.mcrpg.util.blockmeta;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license. Modified 2/16/22
 * It was released under the GPLv3 license
 *
 * Contains blockstore methods that are safe for external plugins to access.
 * An instance can be retrieved via {@link us.eunoians.mcrpg.McRPG#getPlaceStore()}
 */
public interface UserBlockTracker {

    /**
     * Check to see if a given block location is set to true
     *
     * @param block Block location to check
     * @return true if the given block location is set to true, false if otherwise
     */
    boolean isTrue(@NotNull Block block);

    /**
     * Check to see if a given BlockState location is set to true
     *
     * @param blockState BlockState to check
     * @return true if the given BlockState location is set to true, false if otherwise
     */
    boolean isTrue(@NotNull BlockState blockState);

    /**
     * Set a given block location to true
     *
     * @param block Block location to set
     */
    void setTrue(@NotNull Block block);

    /**
     * Set a given BlockState location to true
     *
     * @param blockState BlockState location to set
     */
    void setTrue(@NotNull BlockState blockState);

    /**
     * Set a given block location to false
     *
     * @param block Block location to set
     */
    void setFalse(@NotNull Block block);

    /**
     * Set a given BlockState location to false
     *
     * @param blockState BlockState location to set
     */
    void setFalse(@NotNull BlockState blockState);
}
