package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import us.eunoians.mcrpg.players.PlayerManager;

public class MoveEvent implements Listener {

  @EventHandler (priority = EventPriority.HIGHEST)
  public void playerMove(PlayerMoveEvent e) {
    if (PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())) {
      e.setCancelled(true);
    }
  }
}
