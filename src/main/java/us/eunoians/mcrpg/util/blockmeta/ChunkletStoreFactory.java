package us.eunoians.mcrpg.util.blockmeta;

/**
 * This code is not mine. It is from the original McMMO allowed for use by their license.
 * All credit goes to the original authors as I have only changed a little to suit my needs
 */

public class ChunkletStoreFactory {
  protected static ChunkletStore getChunkletStore(){
	// TODO: Add in loading from config what type of store we want.
	return new PrimitiveExChunkletStore();
  }
}
