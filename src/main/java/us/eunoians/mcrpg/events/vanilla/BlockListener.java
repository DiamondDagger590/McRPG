package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.players.PlayerManager;

import java.util.List;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
 * This code has been modified from it source material
 * It was released under the GPLv3 license
 */

public class BlockListener implements Listener {
  private final McRPG plugin;

  public BlockListener(final McRPG plugin){
    this.plugin = plugin;
  }

  /**
   * Monitor falling blocks.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onFallingBlock(EntityChangeBlockEvent event){

    if(event.getEntityType().equals(EntityType.FALLING_BLOCK)){
      if(event.getTo().equals(Material.AIR) && McRPG.getPlaceStore().isTrue(event.getBlock())){
        event.getEntity().setMetadata("mcMMOBlockFall", new FixedMetadataValue(plugin, event.getBlock().getLocation()));
      }
      else{
        List<MetadataValue> values = event.getEntity().getMetadata("mcMMOBlockFall");

        if(!values.isEmpty()){

          if(values.get(0).value() == null) return;
          Block spawn = ((Location) values.get(0).value()).getBlock();


          McRPG.getPlaceStore().setTrue(event.getBlock());
          McRPG.getPlaceStore().setFalse(spawn);

        }
      }
    }
  }

  /**
   * Monitor BlockPlace events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event){
    Player player = event.getPlayer();

    if(!PlayerManager.isPlayerStored(event.getPlayer().getUniqueId())){
      return;
    }

    BlockState blockState = event.getBlock().getState();

    /* Check if the blocks placed should be monitored so they do not give out XP in the future */
    if(blockState.getType() != Material.CHORUS_FLOWER){
      McRPG.getPlaceStore().setTrue(blockState);
    }
    
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockGrow(BlockGrowEvent event){
    BlockState blockState = event.getBlock().getState();
    McRPG.getPlaceStore().setFalse(blockState);
  }

  /**
   * Monitor BlockBreak events.
   *
   * @param event The event to monitor
   */
  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event){
        /*if (event instanceof FakeBlockBreakEvent) {
            return;
        }*/
    BlockState blockState = event.getBlock().getState();
    Location location = blockState.getLocation();
    Block block = event.getBlock();
    Player p = event.getPlayer();
    //McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
    if(!PlayerManager.isPlayerStored(p.getUniqueId()) || p.getGameMode() == GameMode.CREATIVE){
      return;
    }
    /* Remove metadata from placed watched blocks */
    McRPG.getPlaceStore().setFalse(blockState);
  }
}
