package us.eunoians.mcrpg.util.blockmeta.chunkmeta;

import org.bukkit.World;

/**
 * This code is not mine. It is from the original McMMO allowed for use by their license.
 * All credit goes to the original authors as I have only changed a little to suit my needs
 */

public class ChunkStoreFactory {
  protected static ChunkStore getChunkStore(World world, int x, int z){
	// TODO: Add in loading from config what type of store we want.
	return new PrimitiveChunkStore(world, x, z);
  }
}
