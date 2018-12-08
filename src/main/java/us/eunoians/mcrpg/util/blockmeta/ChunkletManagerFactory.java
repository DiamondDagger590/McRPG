package us.eunoians.mcrpg.util.blockmeta;


import us.eunoians.mcrpg.api.util.HiddenConfig;

/**
 * This code is not mine. It is from the original McMMO allowed for use by their license.
 * All credit goes to the original authors as I have only changed a little to suit my needs
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
