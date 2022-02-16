package us.eunoians.mcrpg.util.blockmeta;

import org.jetbrains.annotations.NotNull;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license. Modified 2/16/22
 * It was released under the GPLv3 license
 */
public class ChunkManagerFactory {

    @NotNull
    public static ChunkManager getChunkManager() {

        if (true) { //PersistentDataConfig.getInstance().useBlockTracker()) {
            return new HashChunkManager();
        }

        return new NullChunkManager();
    }
}
