package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.eunoians.mcrpg.players.PlayerManager;

public class PlayerLoginEvent implements Listener{
  
  @EventHandler(priority = EventPriority.NORMAL)
  public void logInEvent(PlayerJoinEvent e){
    Player p = e.getPlayer();
    if(PlayerLogoutEvent.hasPlayer(p.getUniqueId())){
      PlayerLogoutEvent.cancelRemove(p.getUniqueId());
    }
    else{
      PlayerManager.addMcRPGPlayer(e.getPlayer(), true);
    }
  }
}
