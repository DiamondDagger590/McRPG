package us.eunoians.mcrpg.util.blockmeta.chunkmeta;

import us.eunoians.mcrpg.api.util.HiddenConfig;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
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
