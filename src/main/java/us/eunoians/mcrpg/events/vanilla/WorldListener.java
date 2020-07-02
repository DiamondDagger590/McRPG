package us.eunoians.mcrpg.events.vanilla;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.brewing.BrewingStandManager;
import us.eunoians.mcrpg.util.blockmeta.conversion.BlockStoreConversionMain;

import java.io.File;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
 * This code has been modified from it source material
 * It was released under the GPLv3 license
 */

public class WorldListener implements Listener {
  private final McRPG plugin;
  private static boolean errored = false;

  public WorldListener(final McRPG plugin) {
    this.plugin = plugin;
  }

  /**
   * Monitor StructureGrow events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onStructureGrow(StructureGrowEvent event) {
    if(!McRPG.getPlaceStore().isTrue(event.getLocation().getBlock())) {
      return;
    }

    for(BlockState blockState : event.getBlocks()) {
      McRPG.getPlaceStore().setFalse(blockState);
    }
  }

  /**
   * Monitor WorldInit events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWorldInit(WorldInitEvent event) {
    World world = event.getWorld();

    if(!new File(world.getWorldFolder(), "mcmmo_data").exists() || plugin == null) {
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
  public void onWorldUnload(WorldUnloadEvent event) {
    McRPG.getPlaceStore().unloadWorld(event.getWorld());
  }

  /**
   * Monitor ChunkUnload events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onChunkUnload(ChunkUnloadEvent event) {
    Chunk chunk = event.getChunk();
    BrewingStandManager brewingStandManager = McRPG.getInstance().getBrewingStandManager();
    //Some edge case was happening here
    if(chunk == null || event.getWorld() == null) {
      return;
    }
    if(McRPG.getPlaceStore() != null) {
      McRPG.getPlaceStore().chunkUnloaded(chunk.getX(), chunk.getZ(), event.getWorld());
    }
    else{
      McRPG.resetPlaceStore();
    }
    brewingStandManager.unloadChunk(chunk);
  }

  /**
   * Monitor BlockPistonExtend events.
   *
   * @param event The event to monitor
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPistonExtend(BlockPistonExtendEvent event) {
    BlockFace direction = event.getDirection();
    Block movedBlock = event.getBlock();
    movedBlock = movedBlock.getRelative(direction, 2);

    for(Block b : event.getBlocks()) {
      movedBlock = b.getRelative(direction);
      McRPG.getPlaceStore().setTrue(movedBlock);
    }
  }

  /**
   * Monitor BlockPistonRetract events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPistonRetract(BlockPistonRetractEvent event) {
    // Get opposite direction so we get correct block
    BlockFace direction = event.getDirection();
    Block movedBlock = event.getBlock().getRelative(direction);
    McRPG.getPlaceStore().setTrue(movedBlock);

    for(Block block : event.getBlocks()) {
      movedBlock = block.getRelative(direction);
      McRPG.getPlaceStore().setTrue(movedBlock);
    }
  }

  /**
   * Monitor blocks formed by entities (snowmen)
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityBlockFormEvent(EntityBlockFormEvent event) {
    McRPG.getPlaceStore().setTrue(event.getBlock());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockFormEvent(BlockFormEvent event) {
    McRPG.getPlaceStore().setFalse(event.getBlock());
  }

  /**
   * Monitor BlockPlace events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR)
  public void onBlockPlace(BlockPlaceEvent event) {
    BlockState blockState = event.getBlock().getState();
    McRPG.getPlaceStore().setTrue(blockState);
  }
  
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onBloodPlace(BlockPlaceEvent event){
    if(event.getItemInHand().getType() == Material.REDSTONE){
      NBTItem nbtItem = new NBTItem(event.getItemInHand());
      if(nbtItem.hasKey("McRPGBlood")){
        event.setCancelled(true);
        return;
      }
    }
  }

  /**
   * Monitor BlockMultiPlace events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
    for(BlockState replacedBlockState : event.getReplacedBlockStates()) {
      BlockState blockState = replacedBlockState.getBlock().getState();
      /* Check if the blocks placed should be monitored so they do not give out XP in the future */
      McRPG.getPlaceStore().setTrue(blockState);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockGrow(BlockGrowEvent event) {
    BlockState blockState = event.getBlock().getState();
    McRPG.getPlaceStore().setFalse(blockState);
  }

}
