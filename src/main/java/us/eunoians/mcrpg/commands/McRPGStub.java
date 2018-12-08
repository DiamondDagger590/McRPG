package us.eunoians.mcrpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.util.DiamondFlowersData;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.gui.*;
import us.eunoians.mcrpg.players.McMMOPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.AbilityType;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class McRPGStub implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
	McRPG plugin = McRPG.getInstance();
	if(sender instanceof Player){
	  Player p = (Player) sender;
	  if(args.length == 0){
		McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
		if(mp.hasPendingAbility()){
		  UnlockedAbilities ability = mp.getPendingUnlockAbilities().get(0);
		  if(ability.getAbilityType() == AbilityType.ACTIVE){
			BaseAbility baseAbility = mp.getBaseAbility(ability);
		    if(mp.doesPlayerHaveActiveAbilityFromSkill(Skills.fromString(ability.getSkill()))){
		      BaseAbility oldAbility = mp.getBaseAbility(mp.getActiveAbilityForSkill(Skills.fromString(ability.getSkill())));
			  AbilityOverrideGUI overrideGUI = new AbilityOverrideGUI(mp, oldAbility, baseAbility);
			  p.openInventory(overrideGUI.getGui().getInv());
			  GUITracker.trackPlayer(p, overrideGUI);
			}
			else{
			  GUI gui = new AcceptAbilityGUI(mp, baseAbility, AcceptAbilityGUI.AcceptType.ACCEPT_ABILITY);
			  p.openInventory(gui.getGui().getInv());
			  GUITracker.trackPlayer(p, gui);
			}
		  }
		  else{
			BaseAbility baseAbility = mp.getBaseAbility(ability);
			GUI gui = new AcceptAbilityGUI(mp, baseAbility, AcceptAbilityGUI.AcceptType.ACCEPT_ABILITY);
			p.openInventory(gui.getGui().getInv());
			GUITracker.trackPlayer(p, gui);
		  }
		}
		else{
		  GUI gui = new HomeGUI(PlayerManager.getPlayer(p.getUniqueId()));
		  p.openInventory(gui.getGui().getInv());
		  GUITracker.trackPlayer(p, gui);
		}
		return true;
	  }
	  else if(args.length == 1){
		if(args[0].equalsIgnoreCase("reload")){
		  if(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg.admin.*") || p.hasPermission("mcrpg.admin.reload")){
			McRPG.getInstance().getFileManager().reloadFiles();
			p.sendMessage(Methods.color(plugin.getPluginPrefix() + plugin.getLangFile().getString("Messages.Commands.ReloadFiles")));
			PlayerManager.startSave(plugin);
			DiamondFlowersData.init();
			return true;
		  }
		  else{
			p.sendMessage(Methods.color(plugin.getPluginPrefix() + plugin.getLangFile().getString("Messages.Commands.Utility.NoPerms")));
			return true;
		  }
		}
	  }
	  else{
		p.sendMessage("Not added");
		return true;
	  }
	}
	else{
	  if(args.length == 0){
		sender.sendMessage(Methods.color(plugin.getPluginPrefix() + "&cConsole can not run this command"));
		return true;
	  }
	  else if(args.length == 1){
		if(args[0].equalsIgnoreCase("reload")){
		  McRPG.getInstance().getFileManager().reloadFiles();
		  sender.sendMessage(Methods.color(plugin.getPluginPrefix() + plugin.getLangFile().getString("Messages.Commands.ReloadFiles")));
		  PlayerManager.startSave(plugin);
		  return true;
		}
		else{
		  sender.sendMessage("Not added");
		  return true;
		}
	  }
	  else{
		sender.sendMessage("Not added");
		return true;
	  }
	}
	return false;
  }
}
