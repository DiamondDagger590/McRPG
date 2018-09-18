package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.gui.GUI;
import us.eunoians.mcmmox.gui.GUIEventBinder;
import us.eunoians.mcmmox.gui.GUITracker;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;

public class InvClickEvent implements Listener {

	private static FileConfiguration config;

	public InvClickEvent(Mcmmox plugin){
	  config = plugin.getFileManager().getFile(FileManager.Files.CONFIG);

	}

	//TODO for Diamond to do. Overhaul old system and recreate it to be functional for what we want
	@EventHandler
	public void invClickEvent(InventoryClickEvent e){
		Player p = (Player) e.getWhoClicked();
		if(GUITracker.isPlayerTracked(p)){
			e.setCancelled(true);
			McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
			if(e.getCurrentItem() == null) return;
			GUI currentGUI = GUITracker.getPlayersGUI(p);
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
					if(sender.equalsIgnoreCase("console")) {
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
				else if(event.equalsIgnoreCase("OpenFile")){
					GUITracker.stopTrackingPlayer(p);
					p.closeInventory();
					p.sendMessage(Methods.color("&cThis has yet to be implemented"));
					return;
					/*
					GUIBuilder builder = new GUIBuilder(events[1], events[2], mp);
					*/
				}
			}
		}
	}
}
