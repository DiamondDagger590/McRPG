package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.Bleed;
import us.eunoians.mcmmox.api.events.mcmmo.BleedEvent;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class McMMOBleed implements Listener {

  private static HashMap<UUID, BukkitTask> targetsBleedTasks = new HashMap<>();
  private static HashMap<UUID, ArrayList<UUID>> playersTargeted = new HashMap<>();

  @EventHandler
  public void bleed(BleedEvent e){
    Entity target = e.getTarget();
	McMMOPlayer player = e.getUser();
	Bleed bleed = e.getBleed();
	if(target instanceof Player){
	  McMMOPlayer targ = PlayerManager.getPlayer(target.getUniqueId());
	  if(!bleed.canTarget()){
	    e.setCancelled(true);
	    return;
	  }
	  if(e.isBleedImmunityEnabled()){
	    if(targ.isHasBleedImmunity()){
	      e.setCancelled(true);
	      return;
		}
	  }
	  targ.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.Bleed.PlayerBleeding")));
	}
	startBleedTimer(e);
  }

  public static void cancelPlayer(UUID uuid){
    if(targetsBleedTasks.containsKey(uuid)){
      targetsBleedTasks.get(uuid).cancel();
      targetsBleedTasks.remove(uuid);
      return;
	}
	else{
	  return;
	}
  }

  public static ArrayList<UUID> getPlayersTargetedBy(UUID user){
    if(playersTargeted.containsKey(user)){
      return playersTargeted.get(user);
	}
	else{
	  return new ArrayList<>();
	}
  }

  public static void setPlayersTargetedBy(UUID user, ArrayList<UUID> targets){
    playersTargeted.put(user, targets);
  }

  public static void addPlayerTargetedBy(UUID user, UUID target){
    if(playersTargeted.containsKey(user)){
      playersTargeted.get(user).add(target);
	}
	else{
	  ArrayList<UUID> temp = new ArrayList<>();
	  temp.add(target);
	  playersTargeted.put(user, temp);
	}
  }

  public static void removePlayerTargeted(UUID user, UUID target){
    playersTargeted.get(user).remove(target);
    if(playersTargeted.get(user).isEmpty()){
      playersTargeted.remove(user);
	}
  }

  private void startBleedTimer(BleedEvent e){
    //TODO factor in players buffs
    AtomicInteger iterations = new AtomicInteger((e.getBaseDuration() / e.getFrequency()));
	BukkitTask task = Bukkit.getScheduler().runTaskTimer(Mcmmox.getInstance(), () ->{
	  Entity en = e.getTarget();
	  //If the bleed effect is over
	  if(iterations.get() == 0 || en.isDead() || ((en instanceof  Player) && !((Player)en).isOnline())){
	    if(en instanceof Player && ((Player)en).isOnline()){
		  en.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix()
			  + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.Bleed.BleedingStopped")));
		  McMMOPlayer targ = PlayerManager.getPlayer(e.getTarget().getUniqueId());
		  if(e.isBleedImmunityEnabled()){
			startBleedImmunityTimer(targ, e.getBleedImmunityDuration());
		  }
		}
		if(en instanceof Player){
		  cancelPlayer(e.getTarget().getUniqueId());
		  removePlayerTargeted(e.getUser().getUuid(), e.getTarget().getUniqueId());
		}
	  }
	  else{
	    iterations.decrementAndGet();
	    LivingEntity len = (LivingEntity) en;
		en.getWorld().playEffect(len.getEyeLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
		if(len.getHealth() >= e.getMinimumHealthAllowed()){
		  len.damage(e.getDamage());
		}
	  }
	}, 10, e.getFrequency() * 20);
	if(e.getTarget() instanceof Player){
	  targetsBleedTasks.put(e.getTarget().getUniqueId(), task);
	}
  }

  private static void startBleedImmunityTimer(McMMOPlayer targ, int bleedImmuneDuration){
    if(!targ.isOnline()){
      return;
	}
    targ.setHasBleedImmunity(true);
    Bukkit.getScheduler().runTaskLater(Mcmmox.getInstance(), () ->{
      if(targ.isOnline()){
        targ.setHasBleedImmunity(false);
	  }
	}, bleedImmuneDuration *20);
  }
}
