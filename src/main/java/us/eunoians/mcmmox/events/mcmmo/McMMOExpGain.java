package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.api.events.mcmmo.McMMOPlayerExpGainEvent;

public class McMMOExpGain implements Listener {

  @EventHandler
  public void expGain(McMMOPlayerExpGainEvent e){
	Bukkit.broadcastMessage(Integer.toString(e.getExpGained()));
  }
}
