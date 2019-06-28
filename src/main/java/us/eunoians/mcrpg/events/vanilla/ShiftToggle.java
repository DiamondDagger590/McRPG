package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.swords.RageSpike;
import us.eunoians.mcrpg.api.events.mcrpg.swords.PreRageSpikeEvent;
import us.eunoians.mcrpg.api.events.mcrpg.swords.RageSpikeDamageEvent;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ShiftToggle implements Listener{

  private static HashMap<UUID, Integer> playersCharging = new HashMap<>();

  public static boolean isPlayerCharging(Player p){
    return playersCharging.containsKey(p.getUniqueId());
  }

  public static void removePlayerCharging(Player p){ Bukkit.getScheduler().cancelTask(playersCharging.remove(p));}

  @EventHandler(priority = EventPriority.MONITOR)
  public void shiftToggle(PlayerToggleSneakEvent e){
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())){
      return;
    }
    //Get the player and the McRPGPlayer
    Player player = e.getPlayer();
    McRPGPlayer mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
    //if the player is readying. They cant be charging in this state due to the nature of the below code
    if(mp.isReadying() && e.isSneaking() && mp.getReadyingAbilityBit() != null && mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.RAGE_SPIKE){
      //Fire the pre rage spike event to get and store some values
      PreRageSpikeEvent preEvent = new PreRageSpikeEvent(mp, (RageSpike) mp.getBaseAbility(UnlockedAbilities.RAGE_SPIKE));
      Bukkit.getPluginManager().callEvent(preEvent);
      if(e.isCancelled()){
        return;
      }
      mp.getActiveAbilities().add(UnlockedAbilities.RAGE_SPIKE);
      //Tell the player how long they have to charge for
      player.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() +
              McRPG.getInstance().getLangFile().getString("Messages.Abilities.RageSpike.Charging").replace("%Charge%", Integer.toString(preEvent.getChargeTime()))));
      //Save the id of the task
      int id = new BukkitRunnable(){
        @Override
        public void run(){
          //get vector and make them go voom
          Vector unitVector = new Vector(player.getLocation().getDirection().getX(), 0, player.getLocation().getDirection().getZ());
          player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5, 1);
          //voom code
          e.getPlayer().setVelocity(unitVector.multiply(5));
          e.getPlayer().setVelocity(unitVector.multiply(5));
          //after theyve traveled we need to iteratee 20 times (1 second)
          AtomicInteger count = new AtomicInteger(0);
          //A list of all entities hit by rage spike so we arent double hitting
          ArrayList<UUID> entities = new ArrayList<>();
          //Damage entities as we fly by
          new BukkitRunnable(){
            @Override
            public void run(){
              //verify that this runs 20 times
              if(count.incrementAndGet() == 21){
                cancel();
              }
              else{
                //get all the entities in a 2 by 2 radius
                for(Entity en : player.getNearbyEntities(2, 2, 2)){
                  //if the entity is living (avoids items and such) and isnt already hit
                  if(en instanceof LivingEntity && !(en instanceof ArmorStand) && !entities.contains(en.getUniqueId())){
                    LivingEntity len = (LivingEntity) en;
                    //call the ragespike dmg event
                    RageSpikeDamageEvent event = new RageSpikeDamageEvent(mp, (RageSpike) mp.getBaseAbility(UnlockedAbilities.RAGE_SPIKE), len, preEvent.getDamage());
                    Bukkit.getPluginManager().callEvent(event);
                    if(event.isCancelled()){
                      continue;
                    }
                    //make target go voom
                    Vector targVector = new Vector(en.getLocation().getDirection().getX(), en.getLocation().getDirection().getY(), en.getLocation().getDirection().getZ());
                    en.setVelocity(targVector.multiply(-4.3));
                    //damage target and add them to list
                    len.damage(event.getDamage());
                    entities.add(en.getUniqueId());
                  }
                }
              }
            }
          }.runTaskTimer(McRPG.getInstance(), 0, 1);
          //Get the time we need the cooldown to expire and add it to the McRPGPlayer
          Calendar cal = Calendar.getInstance();
          cal.add(Calendar.SECOND,
                  preEvent.getCooldown());
          mp.getActiveAbilities().remove(UnlockedAbilities.RAGE_SPIKE);
          mp.addAbilityOnCooldown(UnlockedAbilities.RAGE_SPIKE, cal.getTimeInMillis());
          //self remove the task from the array
          playersCharging.remove(player.getUniqueId());
        }
      }.runTaskLater(McRPG.getInstance(), preEvent.getChargeTime() * 20).getTaskId();
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
          player.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Abilities.RageSpike.ChargeCancelled")));
          mp.getActiveAbilities().remove(UnlockedAbilities.RAGE_SPIKE);
          return;
        }
      }
    }
  }
}
