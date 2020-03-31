package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.util.SkullCache;

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
    if(!SkullCache.headMap.containsKey(p.getUniqueId())){
      ItemStack item = new ItemStack(Material.PLAYER_HEAD);
      ItemMeta meta = item.getItemMeta();
      SkullMeta sm = (SkullMeta) meta;
      sm.setOwningPlayer(p);
      item.setItemMeta(sm);
      SkullCache.headMap.put(p.getUniqueId(), item);
    }
  }
}
