package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.api.events.mcmmo.AbilityActivateEvent;

public class AbilityActivate implements Listener {

 @EventHandler
 public void abilityActivateEvent(AbilityActivateEvent e){
   Bukkit.broadcastMessage("1");
 }
}
