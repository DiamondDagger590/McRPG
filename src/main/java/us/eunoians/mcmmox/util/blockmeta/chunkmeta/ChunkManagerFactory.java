package us.eunoians.mcmmox.util.blockmeta.chunkmeta;

import us.eunoians.mcmmox.api.util.HiddenConfig;

/**
 * This code is not mine. It is from the original McMMO allowed for use by their license.
 * All credit goes to the original authors as I have only changed a little to suit my needs
 */

public class ChunkManagerFactory {
  public static ChunkManager getChunkManager(){
	HiddenConfig hConfig = HiddenConfig.getInstance();

	if(hConfig.getChunkletsEnabled()){
	  return new HashChunkManager();
	}

	return new NullChunkManager();
  }
}
