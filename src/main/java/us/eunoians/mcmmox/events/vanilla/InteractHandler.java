package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.abilities.mining.BlastMining;
import us.eunoians.mcmmox.abilities.mining.DoubleDrop;
import us.eunoians.mcmmox.abilities.mining.OreScanner;
import us.eunoians.mcmmox.abilities.mining.SuperBreaker;
import us.eunoians.mcmmox.api.events.mcmmo.BlastMiningEvent;
import us.eunoians.mcmmox.api.events.mcmmo.BlastTestEvent;
import us.eunoians.mcmmox.api.events.mcmmo.OreScannerEvent;
import us.eunoians.mcmmox.api.events.mcmmo.SuperBreakerEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.players.PlayerReadyBit;
import us.eunoians.mcmmox.types.DefaultAbilities;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.Calendar;

public class InteractHandler implements Listener {

  @EventHandler
  public void interactHandler(PlayerInteractEvent e){
	Player p = e.getPlayer();
	McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
	ItemStack heldItem = e.getItem();
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
	  UnlockedAbilities abilityType = bit.getAbilityReady();
	  BaseAbility ability = mp.getSkill(abilityType.getSkill()).getAbility(abilityType);
	  if(abilityType == UnlockedAbilities.BLAST_MINING){
		if(heldItem.getType() == Material.TNT){
		  BlastMining blastMining = (BlastMining) ability;
		  FileConfiguration mining = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
		  e.setCancelled(true);
		  int radius = mining.getInt("BlastMiningConfig.Tier" + Methods.convertToNumeral(blastMining.getCurrentTier()) + ".Radius");
		  int cooldown = mining.getInt("BlastMiningConfig.Tier" + Methods.convertToNumeral(blastMining.getCurrentTier()) + ".Cooldown");
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
			heldItem.setAmount(heldItem.getAmount() - 1);
			if(heldItem.getAmount() <= 0){
			  heldItem.setType(Material.AIR);
			}
			p.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, p.getLocation(), 50);
			p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 10, 1);
			ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE, 1);
			for(Block b : blocks){
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
			mp.addAbilityOnCooldown(UnlockedAbilities.BLAST_MINING, cal.getTimeInMillis());
		  }
		}
		return;
	  }
	  else if(abilityType.equals(UnlockedAbilities.SUPER_BREAKER)){
		SuperBreaker superBreaker = (SuperBreaker) ability;
		FileConfiguration mining = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
		e.setCancelled(true);
		int hasteDuration = mining.getInt("SuperBreakerConfig.Tier" + Methods.convertToNumeral(superBreaker.getCurrentTier()) + ".Duration");
		int cooldown = mining.getInt("SuperBreakerConfig.Tier" + Methods.convertToNumeral(superBreaker.getCurrentTier()) + ".Cooldown");
		double boost = mining.getDouble("SuperBreakerConfig.Tier" + Methods.convertToNumeral(superBreaker.getCurrentTier()) + ".ActivationBoost");
		SuperBreakerEvent superBreakerEvent = new SuperBreakerEvent(mp, superBreaker, cooldown, boost, hasteDuration);
		Bukkit.getPluginManager().callEvent(superBreakerEvent);
		if(superBreakerEvent.isCancelled()){
		  return;
		}
		Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
		mp.setReadyingAbilityBit(null);
		DoubleDrop doubleDrop = (DoubleDrop) mp.getBaseAbility(DefaultAbilities.DOUBLE_DROP);
		doubleDrop.setBonusChance(doubleDrop.getBonusChance() + superBreakerEvent.getBoost());
		PotionEffect effect = new PotionEffect(PotionEffectType.FAST_DIGGING, superBreakerEvent.getHasteDuration() * 20, 6);
		p.addPotionEffect(effect);
		mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.SuperBreaker.Activated")));
		new BukkitRunnable() {
		  @Override
		  public void run(){
			doubleDrop.setBonusChance(0);
			mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
				Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.SuperBreaker.Deactivated")));
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND,
				superBreakerEvent.getCooldown());
			mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_VEX_CHARGE, 10, 1);
			mp.addAbilityOnCooldown(UnlockedAbilities.SUPER_BREAKER, superBreakerEvent.getCooldown());
		  }
		}.runTaskLater(Mcmmox.getInstance(), superBreakerEvent.getHasteDuration() * 20);
		return;
	  }
	  else if(abilityType == UnlockedAbilities.ORE_SCANNER){
		OreScanner oreScanner = (OreScanner) ability;
		FileConfiguration mining = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
		e.setCancelled(true);
		int radius = mining.getInt("OreScannerConfig.Tier" + Methods.convertToNumeral(oreScanner.getCurrentTier()) + ".Radius");
		int cooldown = mining.getInt("OreScannerConfig.Tier" + Methods.convertToNumeral(oreScanner.getCurrentTier()) + ".Cooldown");
		OreScannerEvent oreScannerEvent = new OreScannerEvent(mp, oreScanner, cooldown);
		Bukkit.getPluginManager().callEvent(oreScannerEvent);
		if(oreScannerEvent.isCancelled()){
		  return;
		}
		Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
		mp.setReadyingAbilityBit(null);
		mp.addAbilityOnCooldown(UnlockedAbilities.ORE_SCANNER, oreScannerEvent.getCooldown());
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
		  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			  Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.NothingFound")));
		  return;
		}
		else{
		  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			  Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.PointingToValuable")));
		}
		Location lookAt = null;
		if(goldOre != null){
		  lookAt = goldOre;
		  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			  Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.GoldFound").replace("%Amount%", Integer.toString(goldOreAmount))));
		}
		if(emeraldOre != null){
		  lookAt = emeraldOre;
		  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			  Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.EmeraldsFound").replace("%Amount%", Integer.toString(emeraldOreAmount))));
		}
		if(diamondOre != null){
		  lookAt = diamondOre;
		  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			  Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.DiamondsFound").replace("%Amount%", Integer.toString(diamondOreAmount))));
		}
		p.teleport(Methods.lookAt(p.getLocation(), lookAt));
	  }
	}
  }
}
