package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.herbalism.MassHarvest;
import us.eunoians.mcrpg.abilities.herbalism.PansBlessing;
import us.eunoians.mcrpg.abilities.mining.BlastMining;
import us.eunoians.mcrpg.abilities.mining.DoubleDrop;
import us.eunoians.mcrpg.abilities.mining.OreScanner;
import us.eunoians.mcrpg.abilities.mining.SuperBreaker;
import us.eunoians.mcrpg.api.events.mcrpg.herbalism.MassHarvestEvent;
import us.eunoians.mcrpg.api.events.mcrpg.herbalism.PansBlessingEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.BlastMiningEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.BlastTestEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.OreScannerEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.SuperBreakerEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.players.PlayerReadyBit;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.mcmmo.ItemUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InteractHandler implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void interactHandler(PlayerInteractEvent e){
	Player p = e.getPlayer();
	McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
	ItemStack heldItem = e.getItem();
	if(heldItem == null){
	  return;
	}
	if(e.isCancelled() && e.getAction() == Action.RIGHT_CLICK_AIR){
	  return;
	}
	Block target = e.getClickedBlock();
	Material type;
	if(target == null){
	  type = Material.AIR;
	}
	else{
	  type = target.getType();
	}

	if(mp.isReadying()){
	  PlayerReadyBit bit = mp.getReadyingAbilityBit();
	  if(bit == null){
		mp.setReadying(false);
		return;
	  }
	  UnlockedAbilities abilityType = bit.getAbilityReady();
	  BaseAbility ability = mp.getSkill(abilityType.getSkill()).getAbility(abilityType);
	  if(abilityType == UnlockedAbilities.BLAST_MINING){
		if(heldItem.getType() == Material.TNT){
		  BlastMining blastMining = (BlastMining) ability;
		  FileConfiguration mining = McRPG.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
		  e.setCancelled(true);
		  String key = "BlastMiningConfig.Tier" + Methods.convertToNumeral(blastMining.getCurrentTier());
		  int radius = mining.getInt(key + ".Radius");
		  int cooldown = mining.getInt(key + ".Cooldown");
		  boolean useBlacklist = mining.getBoolean(key + ".UseBlackList");
		  boolean useWhiteList = mining.getBoolean(key + ".UseWhiteList");
		  List<String> blackList = mining.getStringList(key + ".BlackList");
		  List<String> whiteList = mining.getStringList(key + "WhiteList");
		  ArrayList<Block> blocks = new ArrayList<>();
		  for(int x = -1 * radius; x < radius; x++){
			for(int z = -1 * radius; z < radius; z++){
			  for(int y = -1 * radius; y < radius; y++){
				blocks.add(p.getLocation().add(x, y, z).getBlock());
			  }
			}
		  }
		  BlastMiningEvent blastMiningEvent = new BlastMiningEvent(mp, blastMining, blocks, cooldown);
		  Bukkit.getPluginManager().callEvent(blastMiningEvent);
		  if(!blastMiningEvent.isCancelled()){
		    mp.getActiveAbilities().add(UnlockedAbilities.BLAST_MINING);
			heldItem.setAmount(heldItem.getAmount() - 1);
			if(heldItem.getAmount() <= 0){
			  heldItem.setType(Material.AIR);
			}
			p.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, p.getLocation(), 50);
			p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5, 1);
			ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE, 1);
			for(Block b : blocks){
			  Material material = b.getType();
			  if(material == Material.WATER || material == Material.LAVA || material.toString().contains("AIR")){
				continue;
			  }
			  if(useBlacklist && blackList.contains(material.toString())){
				continue;
			  }
			  if(useWhiteList && !whiteList.contains(material.toString())){
				continue;
			  }
			  BlastTestEvent breakEvent = new BlastTestEvent(b, p);
			  if(breakEvent.isCancelled()){
				continue;
			  }
			  b.breakNaturally(pick);
			}
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, blastMiningEvent.getCooldown());
			Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
			mp.setReadyingAbilityBit(null);
			mp.setReadying(false);
			mp.getActiveAbilities().remove(UnlockedAbilities.BLAST_MINING);
			mp.addAbilityOnCooldown(UnlockedAbilities.BLAST_MINING, cal.getTimeInMillis());
		  }
		}
		return;
	  }
	  else if(abilityType.equals(UnlockedAbilities.SUPER_BREAKER) && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR)){
		SuperBreaker superBreaker = (SuperBreaker) ability;
		FileConfiguration mining = McRPG.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
		e.setCancelled(true);
		int hasteDuration = mining.getInt("SuperBreakerConfig.Tier" + Methods.convertToNumeral(superBreaker.getCurrentTier()) + ".Duration");
		int cooldown = mining.getInt("SuperBreakerConfig.Tier" + Methods.convertToNumeral(superBreaker.getCurrentTier()) + ".Cooldown");
		double boost = mining.getDouble("SuperBreakerConfig.Tier" + Methods.convertToNumeral(superBreaker.getCurrentTier()) + ".ActivationBoost");
		SuperBreakerEvent superBreakerEvent = new SuperBreakerEvent(mp, superBreaker, cooldown, boost, hasteDuration);
		Bukkit.getPluginManager().callEvent(superBreakerEvent);
		if(superBreakerEvent.isCancelled()){
		  return;
		}
		mp.getActiveAbilities().add(UnlockedAbilities.SUPER_BREAKER);
		Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
		mp.setReadyingAbilityBit(null);
		mp.setReadying(false);
		DoubleDrop doubleDrop = (DoubleDrop) mp.getBaseAbility(DefaultAbilities.DOUBLE_DROP);
		doubleDrop.setBonusChance(doubleDrop.getBonusChance() + superBreakerEvent.getBoost());
		PotionEffect effect = new PotionEffect(PotionEffectType.FAST_DIGGING, superBreakerEvent.getHasteDuration() * 20, 20);
		p.addPotionEffect(effect);
		mp.getPlayer().sendMessage(Methods.color(p,McRPG.getInstance().getPluginPrefix() +
			McRPG.getInstance().getLangFile().getString("Messages.Abilities.SuperBreaker.Activated")));
		new BukkitRunnable() {
		  @Override
		  public void run(){
			doubleDrop.setBonusChance(0);
			mp.getPlayer().sendMessage(Methods.color(p,McRPG.getInstance().getPluginPrefix() +
				McRPG.getInstance().getLangFile().getString("Messages.Abilities.SuperBreaker.Deactivated")));
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND,
				superBreakerEvent.getCooldown());
			mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_VEX_CHARGE, 10, 1);
			mp.getActiveAbilities().remove(UnlockedAbilities.SUPER_BREAKER);
			mp.addAbilityOnCooldown(UnlockedAbilities.SUPER_BREAKER, cal.getTimeInMillis());
		  }
		}.runTaskLater(McRPG.getInstance(), superBreakerEvent.getHasteDuration() * 20);
		return;
	  }
	  else if(abilityType == UnlockedAbilities.ORE_SCANNER && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR)){
		OreScanner oreScanner = (OreScanner) ability;
		FileConfiguration mining = McRPG.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
		e.setCancelled(true);
		int radius = mining.getInt("OreScannerConfig.Tier" + Methods.convertToNumeral(oreScanner.getCurrentTier()) + ".Radius");
		int cooldown = mining.getInt("OreScannerConfig.Tier" + Methods.convertToNumeral(oreScanner.getCurrentTier()) + ".Cooldown");
		OreScannerEvent oreScannerEvent = new OreScannerEvent(mp, oreScanner, cooldown);
		Bukkit.getPluginManager().callEvent(oreScannerEvent);
		if(oreScannerEvent.isCancelled()){
		  return;
		}
		mp.getActiveAbilities().add(UnlockedAbilities.ORE_SCANNER);
		Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
		mp.setReadyingAbilityBit(null);
		mp.setReadying(false);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, oreScannerEvent.getCooldown());
		mp.addAbilityOnCooldown(UnlockedAbilities.ORE_SCANNER, cal.getTimeInMillis());
		Location goldOre = null;
		Location emeraldOre = null;
		Location diamondOre = null;
		int goldOreAmount = 0;
		int emeraldOreAmount = 0;
		int diamondOreAmount = 0;
		for(int x = -1 * radius; x < radius; x++){
		  for(int z = -1 * radius; z < radius; z++){
			for(int y = -1 * radius; y < radius; y++){
			  Block block = p.getLocation().add(x, y, z).getBlock();
			  Material blockType = block.getType();
			  if(blockType == Material.GOLD_ORE){
				if(goldOre == null){
				  goldOre = block.getLocation();
				}
				else{
				  if(p.getLocation().distance(goldOre) > p.getLocation().distance(block.getLocation())){
					goldOre = block.getLocation();
				  }
				}
				goldOreAmount++;
			  }
			  else if(blockType == Material.EMERALD_ORE){
				if(emeraldOre == null){
				  emeraldOre = block.getLocation();
				}
				else{
				  if(p.getLocation().distance(emeraldOre) > p.getLocation().distance(block.getLocation())){
					emeraldOre = block.getLocation();
				  }
				}
				emeraldOreAmount++;
			  }
			  else if(blockType == Material.DIAMOND_ORE){
				if(diamondOre == null){
				  diamondOre = block.getLocation();
				}
				else{
				  if(p.getLocation().distance(diamondOre) > p.getLocation().distance(block.getLocation())){
					diamondOre = block.getLocation();
				  }
				}
				diamondOreAmount++;
			  }
			}
		  }
		}
		if(diamondOre == null && goldOre == null && emeraldOre == null){
		  p.sendMessage(Methods.color(p,McRPG.getInstance().getPluginPrefix() +
			  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.NothingFound")));
		  return;
		}
		else{
		  p.sendMessage(Methods.color(p,McRPG.getInstance().getPluginPrefix() +
			  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.PointingToValuable")));
		}
		Location lookAt = null;
		if(goldOre != null){
		  lookAt = goldOre;
		  p.sendMessage(Methods.color(p,McRPG.getInstance().getPluginPrefix() +
			  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.GoldFound").replace("%Amount%", Integer.toString(goldOreAmount))));
		}
		if(emeraldOre != null){
		  lookAt = emeraldOre;
		  p.sendMessage(Methods.color(p,McRPG.getInstance().getPluginPrefix() +
			  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.EmeraldsFound").replace("%Amount%", Integer.toString(emeraldOreAmount))));
		}
		if(diamondOre != null){
		  lookAt = diamondOre;
		  p.sendMessage(Methods.color(p,McRPG.getInstance().getPluginPrefix() +
			  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.DiamondsFound").replace("%Amount%", Integer.toString(diamondOreAmount))));
		}
		mp.getActiveAbilities().remove(UnlockedAbilities.ORE_SCANNER);
		p.teleport(Methods.lookAt(p.getLocation(), lookAt));
	  }
	  else if(abilityType == UnlockedAbilities.MASS_HARVEST && (e.getAction() == Action.LEFT_CLICK_BLOCK)){
		if(ItemUtils.isCrop(type)){
		  FileConfiguration herbalism = McRPG.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
		  MassHarvest massHarvest = (MassHarvest) mp.getBaseAbility(UnlockedAbilities.MASS_HARVEST);
		  int radius = herbalism.getInt("MassHarvestConfig.Tier" + Methods.convertToNumeral(massHarvest.getCurrentTier()) + ".Range");
		  ItemStack breakItem = p.getItemInHand().clone();
		  MassHarvestEvent massHarvestEvent = new MassHarvestEvent(mp, massHarvest, radius);
		  Bukkit.getPluginManager().callEvent(massHarvestEvent);
		  if(!massHarvestEvent.isCancelled()){
			mp.getActiveAbilities().add(UnlockedAbilities.MASS_HARVEST);
			Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
			mp.setReadyingAbilityBit(null);
			mp.setReadying(false);
			e.setCancelled(true);
			int cooldown = herbalism.getInt("MassHarvestConfig.Tier" + Methods.convertToNumeral(massHarvest.getCurrentTier()) + ".Cooldown");
			for(int x = -1 * radius; x < radius; x++){
			  for(int z = -1 * radius; z < radius; z++){
				for(int y = -1 * 2; y < 2; y++){
				  Block test = p.getLocation().add(x, y, z).getBlock();
				  Material cropType = test.getType();
				  if(ItemUtils.isCrop(cropType)){
					BlockBreakEvent breakEvent = new BlockBreakEvent(test, p);
					Bukkit.getPluginManager().callEvent(breakEvent);
					if(!breakEvent.isCancelled()){
					  test.breakNaturally(breakItem);
					  if(type == Material.PUMPKIN || type == Material.MELON){
						break;
					  }
					  test.setType(cropType);
					  Ageable ageable = (Ageable) test.getBlockData();
					  ageable.setAge(0);
					  test.setBlockData(ageable);
					}
				  }
				}
			  }
			}
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, cooldown);
			p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.MassHarvest.Activated")));
			mp.getActiveAbilities().remove(UnlockedAbilities.MASS_HARVEST);
			mp.addAbilityOnCooldown(UnlockedAbilities.MASS_HARVEST, cal.getTimeInMillis());
		  }
		}
	  }
	  else if(abilityType == UnlockedAbilities.PANS_BLESSING && e.getAction() == Action.RIGHT_CLICK_BLOCK){
		if(heldItem.getType() == Material.BONE_MEAL){
		  FileConfiguration herbalism = McRPG.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
		  PansBlessing pansBlessing = (PansBlessing) mp.getBaseAbility(UnlockedAbilities.PANS_BLESSING);
		  int radius = herbalism.getInt("PansBlessingConfig.Tier" + Methods.convertToNumeral(pansBlessing.getCurrentTier()) + ".Radius");
		  PansBlessingEvent pansBlessingEvent = new PansBlessingEvent(mp, pansBlessing, radius);
		  Bukkit.getPluginManager().callEvent(pansBlessingEvent);
		  if(!pansBlessingEvent.isCancelled()){
			mp.getActiveAbilities().add(UnlockedAbilities.PANS_BLESSING);
			mp.setReadying(false);
			Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
			mp.setReadyingAbilityBit(null);
			int cooldown = herbalism.getInt("PansBlessingConfig.Tier" + Methods.convertToNumeral(pansBlessing.getCurrentTier()) + ".Cooldown");
			for(int x = -1 * radius; x <= radius; x++){
			  for(int z = -1 * radius; z <= radius; z++){
				for(int y = -1; y <= 1; y++){
				  Block test = p.getLocation().add(x, y, z).getBlock();
				  Material cropType = test.getType();
				  if(ItemUtils.isCrop(cropType)){
					Ageable ageable = (Ageable) test.getBlockData();
					int originalAge = ageable.getMaximumAge();
					ageable.setAge(ageable.getMaximumAge());
					BlockGrowEvent growEvent = new BlockGrowEvent(test, test.getState());
					if(!growEvent.isCancelled()){
					  test.setBlockData(ageable);
					  test.getLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, test.getLocation(), 5);
					}
				  }
				}
			  }
			}
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, cooldown);
			p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.PansBlessing.Activated")));
			mp.getActiveAbilities().add(UnlockedAbilities.PANS_BLESSING);
			mp.addAbilityOnCooldown(UnlockedAbilities.PANS_BLESSING, cal.getTimeInMillis());
		  }
		}
	  }
	}
  }
}
