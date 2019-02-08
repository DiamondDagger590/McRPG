package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.PlayerManager;

import java.util.List;
import java.util.Random;

public class PlayerLoginEvent implements Listener {

  @EventHandler(priority = EventPriority.NORMAL)
  public void logInEvent(PlayerJoinEvent e) {
    Player p = e.getPlayer();
    if(PlayerLogoutEvent.hasPlayer(p.getUniqueId())) {
      PlayerLogoutEvent.cancelRemove(p.getUniqueId());
    }
    else {
      PlayerManager.addMcMMOPlayer(e.getPlayer(), true);
      List<String> possibleMessages = McRPG.getInstance().getLangFile().getStringList("Messages.Tips.LoginTips");
      Random rand = new Random();
      int val = rand.nextInt(possibleMessages.size());
      Bukkit.getScheduler().runTaskLater(McRPG.getInstance(), () -> p.getPlayer().sendMessage(Methods.color(possibleMessages.get(val))), 40L);
    }
  }
}
