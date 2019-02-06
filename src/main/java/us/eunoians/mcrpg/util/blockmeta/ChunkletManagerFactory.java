package us.eunoians.mcrpg.util.blockmeta;


import us.eunoians.mcrpg.api.util.HiddenConfig;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
 */

public class ChunkletManagerFactory {
  public static ChunkletManager getChunkletManager(){
	HiddenConfig hConfig = HiddenConfig.getInstance();

	if(hConfig.getChunkletsEnabled()){
	  return new HashChunkletManager();
	}

	return new NullChunkletManager();
  }
}
