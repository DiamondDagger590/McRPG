package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.gui.GUITracker;
import us.eunoians.mcrpg.gui.PartyBankGUI;
import us.eunoians.mcrpg.players.PlayerManager;

public class InvCloseEvent implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void invClose(InventoryCloseEvent e){
    Player p = (Player) e.getPlayer();
    if(GUITracker.isPlayerTracked(p)){
      if(GUITracker.getPlayersGUI(p) instanceof PartyBankGUI){
        try{
          McRPG.getInstance().getPartyManager().getParty(PlayerManager.getPlayer(p.getUniqueId()).getPartyID()).saveParty();
        }catch(McRPGPlayerNotFoundException ex){
          ex.printStackTrace();
        }
      }
      if(GUITracker.getPlayersGUI(p).isClearData()){
        GUITracker.stopTrackingPlayer(p);
      }
    }
  }
}
