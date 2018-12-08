package us.eunoians.mcrpg.util.blockmeta;

/**
 * This code is not mine. It is from the original McMMO allowed for use by their license.
 * All credit goes to the original authors as I have only changed a little to suit my needs
 */

public class PrimitiveChunkletStore implements ChunkletStore {
  private static final long serialVersionUID = -3453078050608607478L;

  /**
   * X, Z, Y
   */
  public boolean[][][] store = new boolean[16][16][64];

  @Override
  public boolean isTrue(int x, int y, int z){
	return store[x][z][y];
  }

  @Override
  public void setTrue(int x, int y, int z){
	store[x][z][y] = true;
  }

  @Override
  public void setFalse(int x, int y, int z){
	store[x][z][y] = false;
  }

  @Override
  public boolean isEmpty(){
	if(checkEmpty(store)) return false;
	return true;
  }

  static boolean checkEmpty(boolean[][][] store){
	for(int x = 0; x < 16; x++){
	  for(int z = 0; z < 16; z++){
		for(int y = 0; y < 64; y++){
		  if(store[x][z][y]){
			return true;
		  }
		}
	  }
	}
	return false;
  }

  @Override
  public void copyFrom(ChunkletStore otherStore){
	for(int x = 0; x < 16; x++){
	  for(int z = 0; z < 16; z++){
		for(int y = 0; y < 64; y++){
		  store[x][z][y] = otherStore.isTrue(x, y, z);
		}
	  }
	}
  }
}
