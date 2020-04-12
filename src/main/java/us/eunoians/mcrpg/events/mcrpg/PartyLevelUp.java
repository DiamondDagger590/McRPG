package us.eunoians.mcrpg.events.mcrpg;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcrpg.party.PartyLevelUpEvent;
import us.eunoians.mcrpg.api.util.Methods;

import java.util.UUID;

public class PartyLevelUp implements Listener{
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onLevelUp(PartyLevelUpEvent event){
    for(UUID uuid : event.getParty().getAllMemberUUIDs()){
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
      if(offlinePlayer.isOnline()){
        Player player = (Player) offlinePlayer;
        player.sendMessage(Methods.color(player,McRPG.getInstance().getPluginPrefix() + "&aYour party has gained " + (event.getNewLevel() - event.getPreviousLevel()) + " levels."));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
      }
    }
  }
}
