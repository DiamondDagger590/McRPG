package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import us.eunoians.mcmmox.players.PlayerManager;

public class MoveEvent implements Listener {

  @EventHandler
  public void playerMove(PlayerMoveEvent e) {
    if (PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())) {
      e.setCancelled(true);
    }
  }
}
