package us.eunoians.mcrpg.util.blockmeta.conversion;

import org.bukkit.scheduler.BukkitScheduler;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.HiddenConfig;

import java.io.File;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license. Modified 12/7/18
 * It was released under the GPLv3 license
 */

public class BlockStoreConversionXDirectory implements Runnable {
  private int taskID, i;
  private org.bukkit.World world;
  BukkitScheduler scheduler;
  File dataDir;
  File[] zDirs;
  BlockStoreConversionZDirectory[] converters;

  public BlockStoreConversionXDirectory(){
	this.taskID = -1;
  }

  public void start(org.bukkit.World world, File dataDir){
	this.world = world;
	this.scheduler = McRPG.getInstance().getServer().getScheduler();
	this.converters = new BlockStoreConversionZDirectory[HiddenConfig.getInstance().getConversionRate()];
	this.dataDir = dataDir;

	if(this.taskID >= 0){
	  return;
	}

	this.taskID = this.scheduler.runTaskLater(McRPG.getInstance(), this, 1).getTaskId();
	return;
  }

  @Override
  public void run(){
	if(!this.dataDir.exists()){
	  stop();
	  return;
	}

	if(!this.dataDir.isDirectory()){
	  this.dataDir.delete();
	  stop();
	  return;
	}

	if(this.dataDir.listFiles().length <= 0){
	  this.dataDir.delete();
	  stop();
	  return;
	}

	this.zDirs = this.dataDir.listFiles();

	for(this.i = 0; (this.i < HiddenConfig.getInstance().getConversionRate()) && (this.i < this.zDirs.length); this.i++){
	  if(this.converters[this.i] == null){
		this.converters[this.i] = new BlockStoreConversionZDirectory();
	  }

	  this.converters[this.i].start(this.world, this.dataDir, this.zDirs[this.i]);
	}

	stop();
  }

  public void stop(){
	if(this.taskID < 0){
	  return;
	}

	this.scheduler.cancelTask(this.taskID);
	this.taskID = -1;

	this.dataDir = null;
	this.zDirs = null;
	this.world = null;
	this.scheduler = null;
	this.converters = null;
  }
}
