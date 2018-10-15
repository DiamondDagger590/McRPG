package us.eunoians.mcmmox.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.types.GainReason;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;

public class McAdmin implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
	Mcmmox plugin = Mcmmox.getInstance();
	FileConfiguration config = plugin.getLangFile();
	if(sender instanceof Player){
	  Player admin = (Player) sender;
	  if(args.length < 4){
		sendHelpMessage(admin);
	  	return true;
	  }
	  else {
	    if(args[0].equalsIgnoreCase("give")){
	      if(args[1].equalsIgnoreCase("abilitypoints")){
			if(Methods.hasPlayerLoggedInBefore(args[2])){
			  if(!Methods.isInt(args[3])){
			    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
			    return true;
			  }
			  if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.points"))){
			    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
			    return true;
			  }
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
			  int amount = Integer.parseInt(args[3]);
			  if(offlinePlayer.isOnline()){
				McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				mp.setAbilityPoints(mp.getAbilityPoints() + amount);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.AbilityPoints").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
				offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.AbilityPoints").replace("%Amount%", args[3])));
			  	mp.saveData();
				return true;
			  }
			  else{
				McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				mp.setAbilityPoints(mp.getAbilityPoints() + amount);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.AbilityPoints").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
				mp.saveData();
				return true;
			  }
			}
			else{
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
			  return true;
			}
		  }
		  else if(args[1].equalsIgnoreCase("exp")){
		    if(args.length < 5){
		      sendHelpMessage(admin);
		      return true;
			}
			if(Methods.hasPlayerLoggedInBefore(args[2])){
			  if(!Methods.isInt(args[3])){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
				return true;
			  }
			  if(!Skills.isSkill(args[4])){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
				return true;
			  }
			  if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.exp"))){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
				return true;
			  }
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
			  int amount = Integer.parseInt(args[3]);
			  Skills skill = Skills.fromString(args[4]);
			  if(offlinePlayer.isOnline()){
				McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				mp.getSkill(skill).giveExp(amount, GainReason.COMMAND);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Exp")
					.replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getName())));
				offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Skill")
					.replace("%Amount%", args[3]).replace("%Skill%", skill.getName())));
				mp.saveData();
				return true;
			  }
			  else{
				McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				mp.getSkill(skill).giveExp(amount, GainReason.COMMAND);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Exp")
					.replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getName())));				mp.saveData();
				return true;
			  }
			}
			else{
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
			  return true;
			}
		  }
		  else if(args[1].equalsIgnoreCase("level")){
			if(args.length < 5){
			  sendHelpMessage(admin);
			  return true;
			}
			if(Methods.hasPlayerLoggedInBefore(args[2])){
			  if(!Methods.isInt(args[3])){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
				return true;
			  }
			  if(!Skills.isSkill(args[4])){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
				return true;
			  }
			  if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.level"))){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
				return true;
			  }
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
			  int amount = Integer.parseInt(args[3]);
			  Skills skill = Skills.fromString(args[4]);
			  if(offlinePlayer.isOnline()){
				McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				mp.getSkill(skill).giveLevels(amount, true);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Level")
					.replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getName())));
				offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Level")
					.replace("%Amount%", args[3]).replace("%Skill%", skill.getName())));
				mp.saveData();
				return true;
			  }
			  else{
				McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				mp.getSkill(skill).giveLevels(amount, true);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Level")
					.replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getName())));
				mp.saveData();
				return true;
			  }
			}
			else{
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
			  return true;
			}
		  }
		  else if(args[1].equalsIgnoreCase("ability")){
			if(Methods.hasPlayerLoggedInBefore(args[2])){
			  if(!UnlockedAbilities.isAbility(args[3])){
			    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAbility")));
			    return true;
			  }
			  if(!(admin.hasPermission("admin.*") || admin.hasPermission("admin.give.*") || admin.hasPermission("admin.give.ability"))){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
				return true;
			  }
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
			  UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
			  if(offlinePlayer.isOnline()){
				McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				if(mp.getAbilityLoadout().size() == 9){
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.LoadoutFull")));
				  return true;
				}
				BaseAbility baseAbility = mp.getBaseAbility(ability);
				if(!baseAbility.isUnlocked()){
				  baseAbility.setCurrentTier(1);
				}
				baseAbility.setUnlocked(true);
				baseAbility.setToggled(true);
				mp.getAbilityLoadout().add(ability);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Ability").replace("%Player%", offlinePlayer.getName()).replace("%Ability%", ability.getName())));
				offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Ability").replace("%Ability%", ability.getName())));
				mp.saveData();
				return true;
			  }
			  else{
				McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				BaseAbility baseAbility = mp.getBaseAbility(ability);
				if(!baseAbility.isUnlocked()){
				  baseAbility.setCurrentTier(1);
				}
				baseAbility.setUnlocked(true);
				baseAbility.setToggled(true);
				mp.getAbilityLoadout().add(ability);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Ability").replace("%Player%", offlinePlayer.getName()).replace("%Ability%", ability.getName())));
				mp.saveData();
				return true;
			  }
			}
			else{
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
			  return true;
			}
		  }
		}
		else if(args[0].equalsIgnoreCase("replace")){
		  if(Methods.hasPlayerLoggedInBefore(args[1])){
			if(!UnlockedAbilities.isAbility(args[2]) && !UnlockedAbilities.isAbility(args[3])){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAbility")));
			  return true;
			}
			else{
			  if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.replace.*") || admin.hasPermission("mcadmin.replace.ability"))){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
				return true;
			  }
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
			  UnlockedAbilities old = UnlockedAbilities.fromString(args[2]);
			  UnlockedAbilities newAbility = UnlockedAbilities.fromString(args[3]);
			  if(offlinePlayer.isOnline()){
				McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				if(!mp.getAbilityLoadout().contains(old)){
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
				  .replace("%Ability%", old.getName())));
				  return true;
				}
				else{
				  BaseAbility ab = mp.getBaseAbility(newAbility);
				  if(!ab.isUnlocked()){
				    ab.setCurrentTier(1);
				  }
				  ab.setToggled(true);
				  ab.setUnlocked(true);
				  mp.getAbilityLoadout().set(mp.getAbilityLoadout().indexOf(old), newAbility);
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Replaced")
				  .replace("%Old_Ability%", old.getName()).replace("%New_Ability%", newAbility.getName())));
				  offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Replaced")
					  .replace("%Old_Ability%", old.getName()).replace("%New_Ability%", newAbility.getName())));
				  return true;
				}
			  }
			  else{
				McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				if(!mp.getAbilityLoadout().contains(old)){
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
					  .replace("%Ability%", old.getName())));
				  return true;
				}
				else{
				  BaseAbility ab = mp.getBaseAbility(newAbility);
				  if(!ab.isUnlocked()){
					ab.setCurrentTier(1);
				  }
				  ab.setToggled(true);
				  ab.setUnlocked(true);
				  mp.getAbilityLoadout().set(mp.getAbilityLoadout().indexOf(old), newAbility);
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Replaced")
					  .replace("%Old_Ability%", old.getName()).replace("%New_Ability%", newAbility.getName())));
				  return true;
				}
			  }
			}
		  }
		  else{
			admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
			return true;
		  }
		}
		else if(args[0].equalsIgnoreCase("remove")){
		  if(Methods.hasPlayerLoggedInBefore(args[1])){
			if(!UnlockedAbilities.isAbility(args[2])){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAbility")));
			  return true;
			}
			if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.remove.*") || admin.hasPermission("mcadmin.remove.ability"))){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
			  return true;
			}
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
			UnlockedAbilities ability = UnlockedAbilities.fromString(args[2]);
			if(offlinePlayer.isOnline()){
			  McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
			  if(!mp.getAbilityLoadout().contains(ability)){
			    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
				.replace("%Ability%", ability.getName())));
			    return true;
			  }
			  else{
				mp.getAbilityLoadout().remove(ability);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Remove.Ability")
					.replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
				offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Removed.Ability")
					.replace("%Ability%", ability.getName())));
				mp.saveData();
				return true;
			  }
			}
			else{
			  McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
			  if(!mp.getAbilityLoadout().contains(ability)){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
					.replace("%Ability%", ability.getName())));
				return true;
			  }
			  else{
				mp.getAbilityLoadout().remove(ability);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Remove.Ability")
				.replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
				mp.saveData();
				return true;
			  }
			}
		  }
		  else{
			admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
			return true;
		  }

		}
		else if(args[0].equalsIgnoreCase("view")){
		  if(args[1].equalsIgnoreCase("loadout")){

		  }
		}
		else if(args[0].equalsIgnoreCase("cooldown")){

		}
		else if(args[0].equalsIgnoreCase("reset")){

		}
		else{
		  sendHelpMessage(admin);
		  return true;
		}
	  }
	}
	else{

	}
	return false;
  }

  private void sendHelpMessage(CommandSender p){
	Mcmmox plugin = Mcmmox.getInstance();
	FileConfiguration config = plugin.getLangFile();
	p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.HelpPrompt").replaceAll("<command>", "mcadmin")));
  }
}
