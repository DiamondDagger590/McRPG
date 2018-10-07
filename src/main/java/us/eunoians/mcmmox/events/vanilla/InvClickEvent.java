package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.gui.*;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.types.UnlockedAbilities;

public class InvClickEvent implements Listener {

  private FileConfiguration config;

  public InvClickEvent(Mcmmox plugin){
	config = Mcmmox.getInstance().getLangFile();

  }

  //TODO for Diamond to do. Overhaul old system and recreate it to be functional for what we want
  @EventHandler
  public void invClickEvent(InventoryClickEvent e){
	Player p = (Player) e.getWhoClicked();
	if(GUITracker.isPlayerTracked(p)){
	  if(e.getClickedInventory() instanceof PlayerInventory){
	    return;
	  }
	  e.setCancelled(true);
	  McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
	  if(e.getCurrentItem() == null) return;
	  GUI currentGUI = GUITracker.getPlayersGUI(p);
	  if(currentGUI instanceof AcceptAbilityGUI){
	    AcceptAbilityGUI acceptAbilityGUI = (AcceptAbilityGUI) currentGUI;
	    int slot = e.getSlot();
	    if(slot == 16){
		  currentGUI.setClearData(true);
		  p.closeInventory();
		  GUITracker.stopTrackingPlayer(p);
		  return;
		}
	    if(slot == 10 && mp.getAbilityLoadout().size() < 9){
	      mp.addAbilityToLoadout(acceptAbilityGUI.getAbility());
	      mp.removePendingAbilityUnlock((UnlockedAbilities) acceptAbilityGUI.getAbility().getGenericAbility());
	      acceptAbilityGUI.getAbility().setToggled(true);
	      mp.saveData();
	      currentGUI.setClearData(true);
	      p.closeInventory();
	      GUITracker.stopTrackingPlayer(p);
	      p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix()) + config.getString("Messages.Guis.AcceptedAbility").replace("%Ability%", acceptAbilityGUI.getAbility().getGenericAbility().getName()));
	      return;
	    }
		else{
		  return;
		}
	  }
	  else if(currentGUI instanceof EditLoadoutGUI){
	    EditLoadoutGUI editLoadoutGUI = (EditLoadoutGUI) currentGUI;
	    return;
	  }
	  GUIEventBinder binder = currentGUI.getGui().getBoundEvents().stream().filter(guiBinder -> guiBinder.getSlot() == e.getSlot()).findFirst().orElse(null);
	  if(binder == null) return;
	  for(String eventBinder : binder.getBoundEventList()){
		String[] events = eventBinder.split(":");
		String event = events[0];
		if(event.equalsIgnoreCase("Permission")){
		  String perm = events[1];
		  if(!p.hasPermission(perm)){
			currentGUI.setClearData(true);
			p.closeInventory();
			GUITracker.stopTrackingPlayer(p);
			p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
			return;
		  }
		  else{
			continue;
		  }
		}
		else if(event.equalsIgnoreCase("Command")){
		  String sender = events[1];
		  if(sender.equalsIgnoreCase("console")){
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), events[2].replaceAll("%Player%", p.getName()));
			continue;
		  }
		  else if(sender.equalsIgnoreCase("player")){
			p.performCommand(events[2]);
			continue;
		  }
		}
		else if(event.equalsIgnoreCase("close")){
		  GUITracker.stopTrackingPlayer(p);
		  p.closeInventory();
		  continue;
		}
		else if(event.equalsIgnoreCase("back")){
		  if(GUITracker.doesPlayerHavePrevious(p)){
			GUI previousGUI = GUITracker.getPlayersPreviousGUI(p);
			currentGUI.setClearData(false);
			p.openInventory(previousGUI.getGui().getInv());
			GUITracker.replacePlayersGUI(p, previousGUI);
			continue;
		  }
		  else{
			currentGUI.setClearData(true);
			p.closeInventory();
			GUITracker.stopTrackingPlayer(p);
			continue;
		  }
		}
		else if(event.equalsIgnoreCase("Open")){
		  GUITracker.stopTrackingPlayer(p);
		  p.closeInventory();
		  p.sendMessage(Methods.color("&cThis has yet to be implemented"));
		  return;
		}
		else if(event.equalsIgnoreCase("OpenNative")){
		  GUI gui = null;
		  if(events[1].equalsIgnoreCase("EditLoadoutGUI")){
		    gui = new EditLoadoutGUI(mp);
		    currentGUI.setClearData(false);
		    p.openInventory(gui.getGui().getInv());
		    GUITracker.replacePlayersGUI(mp, gui);
		  }
		}
		else if(event.equalsIgnoreCase("OpenFile")){
		  GUI gui = null;
		  if(events[1].equalsIgnoreCase("skillsgui.yml")){
			gui = new SkillGUI(mp);
		  }
		  else{
		    p.sendMessage("Not added yet");
		    p.closeInventory();
		    return;
		  }
		  currentGUI.setClearData(false);
		  p.openInventory(gui.getGui().getInv());
		  GUITracker.replacePlayersGUI(mp, gui);
		  return;
		}
	  }
	}
  }
}
