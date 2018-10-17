package us.eunoians.mcmmox.util.blockmeta;


import us.eunoians.mcmmox.api.util.HiddenConfig;

public class ChunkletManagerFactory {
  public static ChunkletManager getChunkletManager(){
	HiddenConfig hConfig = HiddenConfig.getInstance();

	if(hConfig.getChunkletsEnabled()){
	  return new HashChunkletManager();
	}

	return new NullChunkletManager();
  }
}
