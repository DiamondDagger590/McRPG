package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.eunoians.mcmmox.players.PlayerManager;

public class PlayerLoginEvent implements Listener {

	@EventHandler
	public void logInEvent(PlayerJoinEvent e){
	  Player p = e.getPlayer();
	  if(PlayerLogoutEvent.hasPlayer(p.getUniqueId())){
	    PlayerLogoutEvent.cancelRemove(p.getUniqueId());
	  }
	  else{
		PlayerManager.addMcMMOPlayer(e.getPlayer(), true);
	  }
	}
}
