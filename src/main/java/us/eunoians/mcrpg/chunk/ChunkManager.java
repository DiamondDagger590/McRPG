package us.eunoians.mcrpg.chunk;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license. Modified 2/16/22
 * It was released under the GPLv3 license
 */
public interface ChunkManager extends UserBlockTracker {

    void closeAll();

    void chunkUnloaded(int cx, int cz, @NotNull World world);

    void unloadWorld(@NotNull World world);
}
