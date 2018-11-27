package us.eunoians.mcmmox.events.vanilla;

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
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.util.blockmeta.conversion.BlockStoreConversionMain;

import java.io.File;

public class WorldListener implements Listener {
  private final Mcmmox plugin;

  public WorldListener(final Mcmmox plugin){
	this.plugin = plugin;
  }

  /**
   * Monitor StructureGrow events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onStructureGrow(StructureGrowEvent event){
	if(!Mcmmox.getPlaceStore().isTrue(event.getLocation().getBlock())){
	  return;
	}

	for(BlockState blockState : event.getBlocks()){
	  Mcmmox.getPlaceStore().setFalse(blockState);
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
	Mcmmox.getPlaceStore().unloadWorld(event.getWorld());
  }

  /**
   * Monitor ChunkUnload events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onChunkUnload(ChunkUnloadEvent event){
	Chunk chunk = event.getChunk();
	Mcmmox.getPlaceStore().chunkUnloaded(chunk.getX(), chunk.getZ(), event.getWorld());
  }
}
