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
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.skills.Skill;
import us.eunoians.mcmmox.types.DisplayType;
import us.eunoians.mcmmox.types.GainReason;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.stream.Collectors;

import static us.eunoians.mcmmox.types.Skills.*;

public class McAdmin implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
	Mcmmox plugin = Mcmmox.getInstance();
	FileConfiguration config = plugin.getLangFile();
	if(sender instanceof Player){
	  Player admin = (Player) sender;
	  if(args.length < 3){
		sendHelpMessage(admin);
		return true;
	  }
	  else{
		if(args[0].equalsIgnoreCase("give")){
		  if(args[1].equalsIgnoreCase("abilitypoints")){
			if(args.length < 4){
			  sendHelpMessage(admin);
			  return true;
			}
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
			  if(!isSkill(args[4])){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
				return true;
			  }
			  if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.exp"))){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
				return true;
			  }
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
			  int amount = Integer.parseInt(args[3]);
			  Skills skill = fromString(args[4]);
			  if(offlinePlayer.isOnline()){
				McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				mp.getSkill(skill).giveExp(amount, GainReason.COMMAND);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Exp")
					.replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getName())));
				offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Exp")
					.replace("%Amount%", args[3]).replace("%Skill%", skill.getName())));
				mp.saveData();
				return true;
			  }
			  else{
				McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				mp.getSkill(skill).giveExp(amount, GainReason.COMMAND);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Exp")
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
			  if(!isSkill(args[4])){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
				return true;
			  }
			  if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.level"))){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
				return true;
			  }
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
			  int amount = Integer.parseInt(args[3]);
			  Skills skill = fromString(args[4]);
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
			if(args.length < 4){
			  sendHelpMessage(admin);
			  return true;
			}
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
				if(mp.getAbilityLoadout().contains(ability)){
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.AlreadyHave")));
				  return true;
				}
				BaseAbility baseAbility = mp.getBaseAbility(ability);
				if(!baseAbility.isUnlocked()){
				  baseAbility.setCurrentTier(1);
				}
				if(baseAbility.getCurrentTier() == 0){
				  baseAbility.setCurrentTier(1);
				}
				baseAbility.setUnlocked(true);
				baseAbility.setToggled(true);
				if(baseAbility instanceof RemoteTransfer){
				  System.out.println(baseAbility.getCurrentTier());
				  ((RemoteTransfer) baseAbility).updateBlocks();
				}
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
		  else{
			sendHelpMessage(admin);
			return true;
		  }
		}
		else if(args[0].equalsIgnoreCase("replace")){
		  if(args.length < 4){
			sendHelpMessage(admin);
			return true;
		  }
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
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Replaced").replace("%Player%", offlinePlayer.getName())
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
			if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.view.*") || admin.hasPermission("mcadmin.view.loadout"))){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
			  return true;
			}
			else{
			  if(Methods.hasPlayerLoggedInBefore(args[2])){
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
				if(offlinePlayer.isOnline()){
				  McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				  admin.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + " &aPlayers Loadout:"));
				  mp.getAbilityLoadout().stream().map(ab -> Methods.color("&e" + ab.getName())).forEach(admin::sendMessage);
				  return true;
				}
				else{
				  McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				  admin.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + " &aPlayers Loadout:"));
				  mp.getAbilityLoadout().stream().map(ab -> Methods.color("&e" + ab.getName())).forEach(admin::sendMessage);
				  return true;
				}
			  }
			  else{
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
				return true;
			  }
			}
		  }
		  else if(isSkill(args[1])){
			if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.view.*") || admin.hasPermission("mcadmin.view." + fromString(args[1])
				.getName().toLowerCase()))){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
			  return true;
			}
			else{
			  if(Methods.hasPlayerLoggedInBefore(args[2])){
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
				Skills skill = fromString(args[1]);
				if(offlinePlayer.isOnline()){
				  McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				  Skill skillInfo = mp.getSkill(skill);
				  admin.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + " &aPlayers " + skill.getName() + " Info:"));
				  admin.sendMessage(Methods.color("&eCurrent Level: " + Integer.toString(skillInfo.getCurrentLevel())));
				  admin.sendMessage(Methods.color("&eCurrent Exp: " + Integer.toString(skillInfo.getCurrentExp())));
				  admin.sendMessage(Methods.color("&eExp To Level: " + Integer.toString(skillInfo.getExpToLevel())));
				  skillInfo.getAbilities().stream().map(ability -> Methods.color("&e" + ability.getGenericAbility().getName() + ": Unlocked-" + ability.isUnlocked() + " Tier-" + ability.getCurrentTier())).forEach(admin::sendMessage);
				  return true;
				}
				else{
				  McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				  Skill skillInfo = mp.getSkill(skill);
				  admin.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + " &aPlayers " + skill.getName() + " Info:"));
				  admin.sendMessage(Methods.color("&eCurrent Level: " + Integer.toString(skillInfo.getCurrentLevel())));
				  admin.sendMessage(Methods.color("&eCurrent Exp: " + Integer.toString(skillInfo.getCurrentExp())));
				  admin.sendMessage(Methods.color("&eExp To Level: " + Integer.toString(skillInfo.getExpToLevel())));
				  skillInfo.getAbilities().stream().map(ability -> Methods.color("&e" + ability.getGenericAbility().getName() + ": Unlocked-" + ability.isUnlocked() + " Tier-" + ability.getCurrentTier())).forEach(admin::sendMessage);
				  return true;
				}
			  }
			  else{
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
				return true;
			  }
			}
		  }
		  else{
			admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
			return true;
		  }
		}
		else if(args[0].equalsIgnoreCase("cooldown")){
		  if(args[1].equalsIgnoreCase("set")){
			if(args.length < 5){
			  sendHelpMessage(admin);
			  return true;
			}
			if(!UnlockedAbilities.isAbility(args[3])){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
			  return true;
			}
			else{
			  if(Methods.hasPlayerLoggedInBefore(args[2])){
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
				UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
				if(ability.isPassiveAbility()){
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotActiveAbility")));
				  return true;
				}
				else{
				  if(!Methods.isInt(args[4])){
					admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAInt")));
					return true;
				  }
				  else{
					if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.cooldown.*") || admin.hasPermission("mcadmin.cooldown.set"))){
					  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
					  return true;
					}
					int cooldown = Integer.parseInt(args[4]);
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND, cooldown);
					if(offlinePlayer.isOnline()){
					  McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
					  mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
					  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Set")
						  .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
					  offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.WasSet")
						  .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4])));
					  mp.saveData();
					  return true;
					}
					else{
					  McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
					  mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
					  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Set")
						  .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
					  mp.saveData();
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
		  }
		  else if(args[1].equalsIgnoreCase("remove")){
			if(args.length < 4){
			  sendHelpMessage(admin);
			  return true;
			}
			if(!UnlockedAbilities.isAbility(args[3])){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
			  return true;
			}
			else{
			  if(Methods.hasPlayerLoggedInBefore(args[2])){
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
				UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
				if(ability.isPassiveAbility()){
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotActiveAbility")));
				  return true;
				}
				else{
				  if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.cooldown.*") || admin.hasPermission("mcadmin.cooldown.remove"))){
					admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
					return true;
				  }
				  if(offlinePlayer.isOnline()){
					McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
					if(mp.getCooldown(ability) == -1){
					  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility").replace("%Ability%", ability.getName())));
					  return true;
					}
					mp.removeAbilityOnCooldown(ability);
					admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Remove")
						.replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
					offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Removed")
						.replace("%Ability%", ability.getName())));
					mp.saveData();
					return true;
				  }
				  else{
					McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
					if(mp.getCooldown(ability) == -1){
					  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility").replace("%Ability%", ability.getName())));
					  return true;
					}
					mp.removeAbilityOnCooldown(ability);
					admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Remove")
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
		  }
		  else if(args[1].equalsIgnoreCase("add")){
			if(args.length < 5){
			  sendHelpMessage(admin);
			  return true;
			}
			if(!UnlockedAbilities.isAbility(args[2])){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
			  return true;
			}
			else{
			  if(Methods.hasPlayerLoggedInBefore(args[2])){
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
				UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
				if(ability.isPassiveAbility()){
				  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotActiveAbility")));
				  return true;
				}
				else{
				  if(!Methods.isInt(args[4])){
					admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAInt")));
					return true;
				  }
				  else{
					if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.cooldown.*") || admin.hasPermission("mcadmin.cooldown.add"))){
					  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
					  return true;
					}
					int cooldown = Integer.parseInt(args[4]);
					Calendar cal = Calendar.getInstance();
					if(offlinePlayer.isOnline()){
					  McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
					  long oldCooldown = mp.getCooldown(ability);
					  cal.setTimeInMillis(oldCooldown);
					  cal.add(Calendar.SECOND, cooldown);
					  mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
					  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Add")
						  .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
					  offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Added")
						  .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4])));
					  mp.saveData();
					  return true;
					}
					else{
					  McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
					  long oldCooldown = mp.getCooldown(ability);
					  cal.setTimeInMillis(oldCooldown);
					  cal.add(Calendar.SECOND, cooldown);
					  mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
					  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Add")
						  .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
					  mp.saveData();
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
		  }
		  else{
			sendHelpMessage(admin);
			return true;
		  }
		}
		else if(args[0].equalsIgnoreCase("reset")){
		  if(args[1].equalsIgnoreCase("skill")){
			if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.reset.*") || admin.hasPermission("mcadmin.reset.skill"))){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
			  return true;
			}
			if(args.length < 4){
			  sendHelpMessage(admin);
			  return true;
			}
			if(Methods.hasPlayerLoggedInBefore(args[2])){
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
			  if(!isSkill(args[3])){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
				return true;
			  }
			  Skills skillEnum = fromString(args[3]);
			  if(offlinePlayer.isOnline()){
				McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				Skill skill = mp.getSkill(skillEnum);
				for(BaseAbility baseAbility : skill.getAbilities()){
				  baseAbility.setUnlocked(false);
				  baseAbility.setCurrentTier(0);
				  baseAbility.setToggled(true);
				}
				ArrayList<UnlockedAbilities> toRemove = mp.getAbilityLoadout().stream().filter(ab -> ab.getSkill().equalsIgnoreCase(skill.getName())).collect(Collectors.toCollection(ArrayList::new));
				for(UnlockedAbilities remove : toRemove){
				  mp.getAbilityLoadout().remove(remove);
				}
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.SkillReset")
					.replace("%Skill%", skill.getName()).replace("%Player%", offlinePlayer.getName())));
				offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.SkillWasReset")
					.replace("%Skill%", skill.getName())));
				mp.saveData();
				return true;
			  }
			  else{
				McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				Skill skill = mp.getSkill(skillEnum);
				for(BaseAbility baseAbility : skill.getAbilities()){
				  baseAbility.setUnlocked(false);
				  baseAbility.setCurrentTier(0);
				  baseAbility.setToggled(true);
				}
				ArrayList<UnlockedAbilities> toRemove = mp.getAbilityLoadout().stream().filter(ab -> ab.getSkill().equalsIgnoreCase(skill.getName())).collect(Collectors.toCollection(ArrayList::new));
				for(UnlockedAbilities remove : toRemove){
				  mp.getAbilityLoadout().remove(remove);
				}
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
			if(args.length < 4){
			  sendHelpMessage(admin);
			  return true;
			}
			if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.reset.*") || admin.hasPermission("mcadmin.reset.ability"))){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
			  return true;
			}
			if(Methods.hasPlayerLoggedInBefore(args[2])){
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
			  if(!UnlockedAbilities.isAbility(args[3])){
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
				return true;
			  }
			  UnlockedAbilities abilityEnum = UnlockedAbilities.fromString(args[3]);
			  if(offlinePlayer.isOnline()){
				McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				BaseAbility baseAbility = mp.getBaseAbility(abilityEnum);
				baseAbility.setUnlocked(false);
				baseAbility.setCurrentTier(0);
				baseAbility.setToggled(true);
				UnlockedAbilities abilities = (UnlockedAbilities) baseAbility.getGenericAbility();
				mp.getAbilityLoadout().remove(abilities);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.AbilityReset")
					.replace("%Ability%", abilities.getName()).replace("%Player%", offlinePlayer.getName())));
				offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.AbilityWasReset")
					.replace("%Ability%", abilities.getName())));
				mp.saveData();
				return true;
			  }
			  else{
				McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				BaseAbility baseAbility = mp.getBaseAbility(abilityEnum);
				baseAbility.setUnlocked(false);
				baseAbility.setCurrentTier(0);
				baseAbility.setToggled(true);
				UnlockedAbilities abilities = (UnlockedAbilities) baseAbility.getGenericAbility();
				mp.getAbilityLoadout().remove(abilities);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.AbilityReset")
					.replace("%Ability%", abilities.getName()).replace("%Player%", offlinePlayer.getName())));
				mp.saveData();
				return true;
			  }
			}
			else{
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
			  return true;
			}
		  }
		  else if(args[1].equalsIgnoreCase("player")){
			if(!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.reset.*") || admin.hasPermission("mcadmin.reset.player"))){
			  admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
			  return true;
			}
			if(Methods.hasPlayerLoggedInBefore(args[2])){
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
			  if(offlinePlayer.isOnline()){
				McMMOPlayer mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
				Arrays.stream(values()).forEach(s -> mp.getSkill(s).resetSkill());
				mp.getAbilityLoadout().clear();
				mp.setAbilityPoints(0);
				if(mp.getReadyingAbilityBit() != null){
				  Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
				  mp.setReadyingAbilityBit(null);
				}
				mp.setReadying(false);
				mp.setDisplayType(DisplayType.EXP_SCOREBOARD);
				admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.Player")
					.replace("%Player%", offlinePlayer.getName())));
				offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.PlayerWasReset")));
				mp.saveData();
				return true;
			  }
			  else{
				McMMOPlayer mp = new McMMOPlayer(offlinePlayer.getUniqueId());
				Arrays.stream(values()).forEach(s -> mp.getSkill(s).resetSkill());
				mp.getAbilityLoadout().clear();
				mp.setAbilityPoints(0);
				if(mp.getReadyingAbilityBit() != null){
				  Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
				  mp.setReadyingAbilityBit(null);
				}
				mp.setReadying(false);
				mp.setDisplayType(DisplayType.EXP_SCOREBOARD);
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
		else{
		  sendHelpMessage(admin);
		  return true;
		}
	  }
	}
	else{
	  sender.sendMessage("Im over making commands. Remind me to make this later. .-.");
	  return true;
	}
	return false;
  }

  private void sendHelpMessage(CommandSender p){
	Mcmmox plugin = Mcmmox.getInstance();
	FileConfiguration config = plugin.getLangFile();
	p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.HelpPrompt").replaceAll("<command>", "mcadmin")));
  }
}
