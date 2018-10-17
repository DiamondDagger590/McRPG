package us.eunoians.mcmmox.util.blockmeta.chunkmeta;

import us.eunoians.mcmmox.api.util.HiddenConfig;

public class ChunkManagerFactory {
  public static ChunkManager getChunkManager(){
	HiddenConfig hConfig = HiddenConfig.getInstance();

	if(hConfig.getChunkletsEnabled()){
	  return new HashChunkManager();
	}

	return new NullChunkManager();
  }
}
