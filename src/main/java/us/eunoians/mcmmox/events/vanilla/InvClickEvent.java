package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
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
	//If this is a gui
	if(GUITracker.isPlayerTracked(p)){
	  //Cancel event
	  e.setCancelled(true);
	  //Ignore player inventory
	  if(e.getClickedInventory() instanceof PlayerInventory){
		return;
	  }
	  McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
	  //Cuz null errors are fun
	  if(e.getCurrentItem() == null) return;
	  GUI currentGUI = GUITracker.getPlayersGUI(p);
	  //This gui was hardcoded so hardcoded events are fine :D
	  if(currentGUI instanceof AcceptAbilityGUI){
		AcceptAbilityGUI acceptAbilityGUI = (AcceptAbilityGUI) currentGUI;
		int slot = e.getSlot();
		if(acceptAbilityGUI.getAcceptType() == AcceptAbilityGUI.AcceptType.ACCEPT_ABILITY){
		  if(slot == 16){
			//This is for canceling
			mp.removePendingAbilityUnlock((UnlockedAbilities) acceptAbilityGUI.getAbility().getGenericAbility());
			mp.saveData();
			currentGUI.setClearData(true);
			p.closeInventory();
			GUITracker.stopTrackingPlayer(p);
			return;
		  }
		  if(slot == 10 && mp.getAbilityLoadout().size() < 9){
			//If they accept and their loadout isnt full
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
		  else if(slot == 10){
			//If their loadout is full but they want this ability
			BaseAbility ability = acceptAbilityGUI.getAbility();
			mp.removePendingAbilityUnlock((UnlockedAbilities) ability.getGenericAbility());
			mp.saveData();
			EditLoadoutGUI editLoadoutGUI = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.ABILITY_OVERRIDE, ability);
			currentGUI.setClearData(false);
			GUITracker.replacePlayersGUI(mp, editLoadoutGUI);
			return;
		  }
		  else{
			return;
		  }
		}
		else if(acceptAbilityGUI.getAcceptType() == AcceptAbilityGUI.AcceptType.ACCEPT_UPGRADE){
		  if(slot == 16){
			//This is for canceling
			currentGUI.setClearData(true);
			p.closeInventory();
			GUITracker.stopTrackingPlayer(p);
			return;
		  }
		  if(slot == 10){
		    mp.setAbilityPoints(mp.getAbilityPoints() - 1);
		    acceptAbilityGUI.getAbility().setCurrentTier(acceptAbilityGUI.getAbility().getCurrentTier() + 1);
		    p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 10, 1);
			mp.saveData();
			currentGUI.setClearData(true);
			p.closeInventory();
			GUITracker.stopTrackingPlayer(p);
			p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + config.getString("Messages.Guis.UpgradedAbility").replace("%Ability%", acceptAbilityGUI.getAbility().getGenericAbility().getName())
			.replace("%Tier%", "Tier " + Methods.convertToNumeral(acceptAbilityGUI.getAbility().getCurrentTier()))));
			return;
		  }
		  else{
			return;
		  }
		}
	  }
	  else if(currentGUI instanceof EditLoadoutGUI){
		EditLoadoutGUI editLoadoutGUI = (EditLoadoutGUI) currentGUI;
		BaseAbility abilityToChange = editLoadoutGUI.getAbilityFromSlot(e.getSlot());
		if(editLoadoutGUI.getEditType() == EditLoadoutGUI.EditType.TOGGLE){
		  abilityToChange.setToggled(!abilityToChange.isToggled());
		  if(!abilityToChange.isToggled()){
			e.getCurrentItem().removeEnchantment(Enchantment.DURABILITY);
		  }
		  else{
			e.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		  }
		}
		else if(editLoadoutGUI.getEditType() == EditLoadoutGUI.EditType.ABILITY_UPGRADE){
		  UnlockedAbilities unlockedAbility = (UnlockedAbilities) abilityToChange.getGenericAbility();
		  if(abilityToChange.getCurrentTier() < 5){
		    if(unlockedAbility.tierUnlockLevel(abilityToChange.getCurrentTier() + 1) > mp.getSkill(unlockedAbility.getSkill()).getCurrentLevel()){
		      p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
		      return;
			}
		    AcceptAbilityGUI gui = new AcceptAbilityGUI(mp, abilityToChange, AcceptAbilityGUI.AcceptType.ACCEPT_UPGRADE);
		    currentGUI.setClearData(false);
		    GUITracker.replacePlayersGUI(mp, gui);
		    p.openInventory(gui.getGui().getInv());
			return;
		  }
		  else{
		    p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
		    return;
		  }
		}
		else{
		  //TODO revist this later
		  editLoadoutGUI.getAbilities().set(e.getSlot(), editLoadoutGUI.getReplaceAbility());
		  mp.getAbilityLoadout().set(e.getSlot(), editLoadoutGUI.getReplaceAbility());
		  editLoadoutGUI.setClearData(true);
		  GUITracker.stopTrackingPlayer(p);
		  p.closeInventory();
		  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix()) + config.getString("Messages.Guis.AcceptedAbility").replace("%Ability%", editLoadoutGUI.getReplaceAbility().getGenericAbility().getName()));
		}
		mp.saveData();
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
			gui = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.TOGGLE);
			currentGUI.setClearData(false);
			p.openInventory(gui.getGui().getInv());
			GUITracker.replacePlayersGUI(mp, gui);
			return;
		  }
		  if(events[1].equalsIgnoreCase("UpgradeAbilityGUI")){
		    if(mp.getAbilityPoints() == 0){
		      p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
		      return;
			}
		    gui = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.ABILITY_UPGRADE);
		    currentGUI.setClearData(false);
		    p.openInventory(gui.getGui().getInv());
		    GUITracker.replacePlayersGUI(mp, gui);
		    return;
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
