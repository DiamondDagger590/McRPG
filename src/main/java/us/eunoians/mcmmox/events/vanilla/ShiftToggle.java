package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ShiftToggle implements Listener {

  private static HashMap<UUID, Integer> playersCharging = new HashMap<>();

  @EventHandler(priority = EventPriority.MONITOR)
  public void shiftToggle(PlayerToggleSneakEvent e){
    Player player = e.getPlayer();
	McMMOPlayer mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
	//if the player is readying. They cant be charging in this state due to the nature of the below code
	if(mp.isReadying() && e.isSneaking()){
		//TODO add the checking stuff
	  player.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
		  Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RageSpike.Charging").replace("%Charge%", Integer.toString(Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("RageSpikeConfig.Tier"
			  + Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.RAGE_SPIKE).getCurrentTier()) + ".ChargeTime")))));
	  //Save the id of the task
	  	int id = Bukkit.getScheduler().runTaskLater(Mcmmox.getInstance(), () ->{
		  //get vector and make them go voom
		  //save their start point
		  Location start = player.getLocation();
		  Vector unitVector = new Vector(player.getLocation().getDirection().getX(), 0, player.getLocation().getDirection().getZ());
		  player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 10, 1);
		  //voom code
		  e.getPlayer().setVelocity(unitVector.multiply(5));
		  e.getPlayer().setVelocity(unitVector.multiply(5));
		  //after theyve traveled
		  AtomicInteger count = new AtomicInteger(0);
		  ArrayList<UUID> entities = new ArrayList<>();
		  //Damage entities as we fly by
		  new BukkitRunnable() {
			@Override
			public void run(){
			  if(count.incrementAndGet() == 21){
			    cancel();
			  }
			  else{
			    for(Entity en : player.getNearbyEntities(2, 2, 2)){
			      if(en instanceof LivingEntity && !entities.contains(en.getUniqueId())){
			        LivingEntity len = (LivingEntity) en;
			        Vector targVector = new Vector(en.getLocation().getDirection().getX(), -2, player.getLocation().getDirection().getZ());
			        en.setVelocity(targVector.multiply(-5));
			        len.damage(Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getDouble("RageSpikeConfig.Tier" +
						Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.RAGE_SPIKE).getCurrentTier()) + ".Damage"));
			        entities.add(en.getUniqueId());
				  }
				}
			  }
			}
		  }.runTaskTimer(Mcmmox.getInstance(), 0, 1);
		  Calendar cal = Calendar.getInstance();
		  cal.add(Calendar.SECOND,
			  Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("RageSpikeConfig.Tier"
				  + Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.RAGE_SPIKE).getCurrentTier()) + ".Cooldown"));
		  mp.addAbilityOnCooldown(UnlockedAbilities.RAGE_SPIKE, cal.getTimeInMillis());
		  //self remove the task from the array
		  playersCharging.remove(player.getUniqueId());
		}, Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("RageSpikeConfig.Tier"
			+ Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.RAGE_SPIKE).getCurrentTier()) + ".ChargeTime") * 20).getTaskId();
	  	//store the task
	  	playersCharging.put(player.getUniqueId(), id);
	  	//disable the readying
	  	mp.setReadying(false);
	  	Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
		mp.setReadyingAbilityBit(null);
	}
	//If a player is in their charge phase
	else{
	  if(isPlayerCharging(player)){
	    if(!e.isSneaking()){
	      Bukkit.getScheduler().cancelTask(playersCharging.remove(player.getUniqueId()));
	      player.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			  Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RageSpike.ChargeCancelled")));
	      return;
		}
	  }
	}
  }

  public static boolean isPlayerCharging(Player p){
    return playersCharging.containsKey(p.getUniqueId());
  }

  public static void removePlayerCharging(Player p) { Bukkit.getScheduler().cancelTask(playersCharging.remove(p));}
}
