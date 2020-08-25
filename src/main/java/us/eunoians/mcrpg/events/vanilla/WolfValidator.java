package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Calendar;

public class WolfValidator implements Listener{
  
  @EventHandler(priority = EventPriority.LOWEST)
  public void handleBreed(EntityBreedEvent entityBreedEvent){
    
    //Don't allow breeding of hell hounds
    if(entityBreedEvent.getFather() instanceof Wolf){
      Wolf wolf = (Wolf) entityBreedEvent.getFather();
      
      //Check if it is a hell hound
      if(wolf.getPersistentDataContainer().has(VanillaDamageEvent.HELL_HOUND_KEY, PersistentDataType.STRING)){
        entityBreedEvent.setCancelled(true);
        Calendar calendar = Calendar.getInstance();
        
        //If for some reason the wolf should've already self destructed, remove it
        if(calendar.getTimeInMillis() >= wolf.getPersistentDataContainer().get(VanillaDamageEvent.HELL_HOUND_SELF_DESTRUCT_KEY, PersistentDataType.LONG)){
          wolf.remove();
        }
      }
    }
    
    //Check the mother
    if(entityBreedEvent.getMother() instanceof Wolf){
      Wolf wolf = (Wolf) entityBreedEvent.getMother();
  
      //Check if it is a hell hound
      if(wolf.getPersistentDataContainer().has(VanillaDamageEvent.HELL_HOUND_KEY, PersistentDataType.STRING)){
        entityBreedEvent.setCancelled(true);
        Calendar calendar = Calendar.getInstance();
  
        //If for some reason the wolf should've already self destructed, remove it
        if(calendar.getTimeInMillis() >= wolf.getPersistentDataContainer().get(VanillaDamageEvent.HELL_HOUND_SELF_DESTRUCT_KEY, PersistentDataType.LONG)){
          wolf.remove();
        }
      }
    }
    
    //No need to check for CallOfWild tags if hell hound tags already cancelled
    if(entityBreedEvent.isCancelled()){
      return;
    }
    else{
      if(entityBreedEvent.getFather().getPersistentDataContainer().has(CallOfWildListener.ANTI_BREEDABLE_KEY, PersistentDataType.STRING)){
        entityBreedEvent.setCancelled(true);
        return;
      }
      else if(entityBreedEvent.getMother().getPersistentDataContainer().has(CallOfWildListener.ANTI_BREEDABLE_KEY, PersistentDataType.STRING)){
        entityBreedEvent.setCancelled(true);
        return;
      }
    }
  }
  
  @EventHandler(priority = EventPriority.LOWEST)
  public void handleAttack(EntityDamageByEntityEvent entityDamageByEntityEvent){
    Entity entity = entityDamageByEntityEvent.getEntity();
    Entity damager = entityDamageByEntityEvent.getDamager();
    if(entity instanceof Wolf){
      if(entity.getPersistentDataContainer().has(VanillaDamageEvent.HELL_HOUND_KEY, PersistentDataType.STRING)){
        Calendar calendar = Calendar.getInstance();
        if(calendar.getTimeInMillis() > entity.getPersistentDataContainer().get(VanillaDamageEvent.HELL_HOUND_SELF_DESTRUCT_KEY, PersistentDataType.LONG)){
          entity.remove();
          entityDamageByEntityEvent.setCancelled(true);
        }
      }
    }
    if(damager instanceof Wolf){
      if(damager.getPersistentDataContainer().has(VanillaDamageEvent.HELL_HOUND_KEY, PersistentDataType.STRING)){
        Calendar calendar = Calendar.getInstance();
        if(calendar.getTimeInMillis() > damager.getPersistentDataContainer().get(VanillaDamageEvent.HELL_HOUND_SELF_DESTRUCT_KEY, PersistentDataType.LONG)){
          damager.remove();
          entityDamageByEntityEvent.setCancelled(true);
        }
      }
    }
  }
  
  @EventHandler(priority = EventPriority.LOWEST)
  public void handleChunkUnload(ChunkUnloadEvent chunkUnloadEvent){
    for(Entity entity : chunkUnloadEvent.getChunk().getEntities()){
      if(entity instanceof Wolf){
        if(entity.getPersistentDataContainer().has(VanillaDamageEvent.HELL_HOUND_KEY, PersistentDataType.STRING)){
          Calendar calendar = Calendar.getInstance();
          if(calendar.getTimeInMillis() > entity.getPersistentDataContainer().get(VanillaDamageEvent.HELL_HOUND_SELF_DESTRUCT_KEY, PersistentDataType.LONG)){
            entity.remove();
          }
        }
      }
    }
  }
}
