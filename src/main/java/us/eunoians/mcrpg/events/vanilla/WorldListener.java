package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.util.blockmeta.conversion.BlockStoreConversionMain;

import java.io.File;

public class WorldListener implements Listener {
  private final McRPG plugin;

  public WorldListener(final McRPG plugin){
	this.plugin = plugin;
  }

  /**
   * Monitor StructureGrow events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onStructureGrow(StructureGrowEvent event){
	if(!McRPG.getPlaceStore().isTrue(event.getLocation().getBlock())){
	  return;
	}

	for(BlockState blockState : event.getBlocks()){
	  McRPG.getPlaceStore().setFalse(blockState);
	}
  }

  /**
   * Monitor WorldInit events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWorldInit(WorldInitEvent event){
	World world = event.getWorld();

	if(!new File(world.getWorldFolder(), "mcmmo_data").exists() || plugin == null){
	  return;
	}

	plugin.getLogger().info("Converting block storage for " + world.getName() + " to a new format.");

	new BlockStoreConversionMain(world).run();
  }

  /**
   * Monitor WorldUnload events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWorldUnload(WorldUnloadEvent event){
	McRPG.getPlaceStore().unloadWorld(event.getWorld());
  }

  /**
   * Monitor ChunkUnload events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onChunkUnload(ChunkUnloadEvent event){
	Chunk chunk = event.getChunk();
	McRPG.getPlaceStore().chunkUnloaded(chunk.getX(), chunk.getZ(), event.getWorld());
  }
}
