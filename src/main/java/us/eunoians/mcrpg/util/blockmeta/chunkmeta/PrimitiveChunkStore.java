package us.eunoians.mcrpg.util.blockmeta.chunkmeta;

import org.bukkit.Bukkit;
import org.bukkit.World;
import us.eunoians.mcrpg.util.blockmeta.ChunkletStore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
 */

public class PrimitiveChunkStore implements ChunkStore {
  private static final long serialVersionUID = -1L;
  transient private boolean dirty = false;
  /**
   * X, Z, Y
   */
  public boolean[][][] store;
  private static final int CURRENT_VERSION = 7;
  private static final int MAGIC_NUMBER = 0xEA5EDEBB;
  private int cx;
  private int cz;
  private UUID worldUid;

  public PrimitiveChunkStore(World world, int cx, int cz){
	this.cx = cx;
	this.cz = cz;
	this.worldUid = world.getUID();
	this.store = new boolean[16][16][world.getMaxHeight()];
  }

  @Override
  public boolean isDirty(){
	return dirty;
  }

  @Override
  public void setDirty(boolean dirty){
	this.dirty = dirty;
  }

  @Override
  public int getChunkX(){
	return cx;
  }

  @Override
  public int getChunkZ(){
	return cz;
  }

  @Override
  public boolean isTrue(int x, int y, int z){
	return store[x][z][y];
  }

  @Override
  public void setTrue(int x, int y, int z){
	if(y >= store[0][0].length || y < 0)
	  return;
	store[x][z][y] = true;
	dirty = true;
  }

  @Override
  public void setFalse(int x, int y, int z){
	if(y >= store[0][0].length || y < 0)
	  return;
	store[x][z][y] = false;
	dirty = true;
  }

  @Override
  public boolean isEmpty(){
	for(int x = 0; x < 16; x++){
	  for(int z = 0; z < 16; z++){
		for(int y = 0; y < store[0][0].length; y++){
		  if(store[x][z][y]){
			return false;
		  }
		}
	  }
	}
	return true;
  }

  @Override
  public void copyFrom(ChunkletStore otherStore){
	for(int x = 0; x < 16; x++){
	  for(int z = 0; z < 16; z++){
		for(int y = 0; y < store[0][0].length; y++){
		  store[x][z][y] = otherStore.isTrue(x, y, z);
		}
	  }
	}
	dirty = true;
  }

  private void writeObject(ObjectOutputStream out) throws IOException{
	out.writeInt(MAGIC_NUMBER);
	out.writeInt(CURRENT_VERSION);

	out.writeLong(worldUid.getLeastSignificantBits());
	out.writeLong(worldUid.getMostSignificantBits());
	out.writeInt(cx);
	out.writeInt(cz);
	out.writeObject(store);

	dirty = false;
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
	int magic = in.readInt();
	// Can be used to determine the format of the file
	int fileVersionNumber = in.readInt();

	if(magic != MAGIC_NUMBER){
	  fileVersionNumber = 0;
	}

	long lsb = in.readLong();
	long msb = in.readLong();
	worldUid = new UUID(msb, lsb);
	cx = in.readInt();
	cz = in.readInt();

	store = (boolean[][][]) in.readObject();

	if(fileVersionNumber < 5){
	  fixArray();
	  dirty = true;
	}
  }

  private void fixArray(){
	boolean[][][] temp = this.store;
	this.store = new boolean[16][16][Bukkit.getWorld(worldUid).getMaxHeight()];
	for(int x = 0; x < 16; x++){
	  for(int z = 0; z < 16; z++){
		for(int y = 0; y < store[0][0].length; y++){
		  try{
			store[x][z][y] = temp[x][y][z];
		  }catch(Exception e){
		  }
		}
	  }
	}
  }
}
