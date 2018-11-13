package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.util.mcmmo.BlockUtils;

import java.util.List;

public class BlockListener implements Listener {
  private final Mcmmox plugin;

  public BlockListener(final Mcmmox plugin){
	this.plugin = plugin;
  }

  /**
   * Monitor BlockPistonExtend events.
   *
   * @param event The event to monitor
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPistonExtend(BlockPistonExtendEvent event){
	BlockFace direction = event.getDirection();
	Block movedBlock = event.getBlock();
	movedBlock = movedBlock.getRelative(direction, 2);

	for(Block b : event.getBlocks()){
	  if(Mcmmox.getPlaceStore().isTrue(b)){
		movedBlock = b.getRelative(direction);
		Mcmmox.getPlaceStore().setTrue(movedBlock);
	  }
	}
  }

  /**
   * Monitor BlockPistonRetract events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPistonRetract(BlockPistonRetractEvent event){
	// Get opposite direction so we get correct block
	BlockFace direction = event.getDirection();
	Block movedBlock = event.getBlock().getRelative(direction);
	Mcmmox.getPlaceStore().setTrue(movedBlock);

	for(Block block : event.getBlocks()){
	  movedBlock = block.getRelative(direction);
	  Mcmmox.getPlaceStore().setTrue(movedBlock);
	}
  }

  /**
   * Monitor falling blocks.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onFallingBlock(EntityChangeBlockEvent event){

	if(event.getEntityType().equals(EntityType.FALLING_BLOCK)){
	  if(event.getTo().equals(Material.AIR) && Mcmmox.getPlaceStore().isTrue(event.getBlock())){
		event.getEntity().setMetadata("mcMMOBlockFall", new FixedMetadataValue(plugin, event.getBlock().getLocation()));
	  }
	  else{
		List<MetadataValue> values = event.getEntity().getMetadata("mcMMOBlockFall");

		if(!values.isEmpty()){

		  if(values.get(0).value() == null) return;
		  Block spawn = ((Location) values.get(0).value()).getBlock();


		  Mcmmox.getPlaceStore().setTrue(event.getBlock());
		  Mcmmox.getPlaceStore().setFalse(spawn);

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
	  Mcmmox.getPlaceStore().setTrue(blockState);
	}

	McMMOPlayer mcMMOPlayer = PlayerManager.getPlayer(player.getUniqueId());

        /*if (blockState.getType() == Repair.anvilMaterial && SkillType.REPAIR.getPermissions(player)) {
            mcMMOPlayer.getRepairManager().placedAnvilCheck();
        }
        else if (blockState.getType() == Salvage.anvilMaterial && SkillType.SALVAGE.getPermissions(player)) {
            mcMMOPlayer.getSalvageManager().placedAnvilCheck();
        }*/
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockGrow(BlockGrowEvent event){
	BlockState blockState = event.getBlock().getState();
	Mcmmox.getPlaceStore().setFalse(blockState);
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
/*
        /* ALCHEMY - Cancel any brew in progress for that BrewingStand
        if (blockState instanceof BrewingStand && Alchemy.brewingStandMap.containsKey(location)) {
            Alchemy.brewingStandMap.get(location).cancelBrew();
        }
*/
	Block block = event.getBlock();
	Player p = event.getPlayer();
	McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
	FileConfiguration mining = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
	if(!PlayerManager.isPlayerStored(p.getUniqueId()) || p.getGameMode() == GameMode.CREATIVE){
	  return;
	}
	/* Remove metadata from placed watched blocks */
	  Mcmmox.getPlaceStore().setFalse(blockState);
  }

  /**
   * Monitor BlockDamage events.
   *
   * @param event The event to watch
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockDamage(BlockDamageEvent event){
	Player player = event.getPlayer();

	if(!PlayerManager.isPlayerStored(player.getUniqueId())){
	  return;
	}

	McMMOPlayer mcMMOPlayer = PlayerManager.getPlayer(player.getUniqueId());
	BlockState blockState = event.getBlock().getState();

	/*
	 * ABILITY PREPARATION CHECKS
	 *
	 * We check permissions here before processing activation.
	 */
	if(BlockUtils.canActivateAbilities(blockState)){
	  ItemStack heldItem = player.getInventory().getItemInMainHand();
	}
//TODO
        /*
         * TREE FELLER SOUNDS
         *
         * We don't need to check permissions here because they've already been checked for the ability to even activate.
         *
        if (mcMMOPlayer.getAbilityMode(AbilityType.TREE_FELLER) && BlockUtils.isLog(blockState) && Config.getInstance().getTreeFellerSoundsEnabled()) {
            player.playSound(blockState.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, Misc.FIZZ_VOLUME, Misc.getFizzPitch());
        }*/
  }

  /**
   * Handle BlockDamage events where the event is modified.
   *
   * @param event The event to modify
   *
   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
   public void onBlockDamageHigher(BlockDamageEvent event) {

   Player player = event.getPlayer();

   if (!PlayerManager.isPlayerStored(player.getUniqueId())) {
   return;
   }

   McMMOPlayer mcMMOPlayer = PlayerManager.getPlayer(player.getUniqueId());
   ItemStack heldItem = player.getInventory().getItemInMainHand();
   Block block = event.getBlock();
   BlockState blockState = block.getState();

   }*/
}
