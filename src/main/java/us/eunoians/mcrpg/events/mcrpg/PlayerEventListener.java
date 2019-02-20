package us.eunoians.mcrpg.events.mcrpg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcrpg.PlayerModifiedEvent;

public class PlayerEventListener implements Listener {

  @EventHandler
  public void mcrpgEvent(PlayerModifiedEvent e){
    Player p = e.getMcRPGPlayer().getPlayer();
    if(p.isOnline()){
      String world = p.getWorld().getName();
      if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
              McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world)){
        e.setCancelled(true);
      }
    }
  }
}
