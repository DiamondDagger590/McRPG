package us.eunoians.mcrpg.util.blockmeta;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
 */

public class ChunkletStoreFactory {
  protected static ChunkletStore getChunkletStore(){
	// TODO: Add in loading from config what type of store we want.
	return new PrimitiveExChunkletStore();
  }
}
