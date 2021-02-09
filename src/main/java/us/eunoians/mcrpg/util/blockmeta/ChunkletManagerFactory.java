package us.eunoians.mcrpg.util.blockmeta;


/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license. Modified 12/7/18
 * It was released under the GPLv3 license
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
