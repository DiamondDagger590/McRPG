package us.eunoians.mcrpg.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.List;

public class TabComplete implements TabCompleter{

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String commandLable, String[] args){
		List<String> completions = new ArrayList<>();
		if(!(sender instanceof Player)){
			return completions;
		}
		Player player = (Player) sender;
		if(args.length == 1){
			switch(args[0].toLowerCase()){
				case "mcrpg":
					if(player.hasPermission("mcrpg.*") || player.hasPermission("mcrpg.reload")){
						completions.add("reload");
					}
				case "mcdisplay":
					for(Skills s : Skills.values()){
						completions.add(s.getDisplayName());
					}
					completions.add("clear");
				case "mcrank":
					for(Skills s : Skills.values()){
						completions.add(s.getDisplayName());
					}
					completions.add("power");
				case "mcredeem":
					for(Skills s : Skills.values()){
						completions.add(s.getDisplayName());
					}
				case "mcadmin":
					completions.add("give");
					completions.add("replace");
					completions.add("remove");
					completions.add("view");
					completions.add("cooldown");
					completions.add("reset");
			}
		}
		else if(args.length == 2){
			if(args[0].equalsIgnoreCase("mcadmin")){
				switch(args[1].toLowerCase()){
					case "give":
						completions.add("abilitypoints");
						completions.add("exp");
						completions.add("level");
						completions.add("ability");
					case "replace":
						for(Player p : Bukkit.getOnlinePlayers()){
							completions.add(p.getName());
						}
					case "remove":
						for(Player p : Bukkit.getOnlinePlayers()){
							completions.add(p.getName());
						}
					case "view":
						for(Skills s : Skills.values()){
							completions.add(s.getDisplayName());
						}
						completions.add("loadout");
					case "cooldown":
						completions.add("set");
						completions.add("remove");
						completions.add("add");
					case "reset":
						completions.add("skill");
						completions.add("ability");
						completions.add("player");
				}
			}
		}
		else if(args.length == 3){
			if(args[0].equalsIgnoreCase("mcadmin")){
				if(args[1].equalsIgnoreCase("give")){
					switch(args[2].toLowerCase()){
						case "exp":
							for(Player p : Bukkit.getOnlinePlayers()){
								completions.add(p.getName());
							}
						case "abilitypoints":
							for(Player p : Bukkit.getOnlinePlayers()){
								completions.add(p.getName());
							}
						case "level":
							for(Player p : Bukkit.getOnlinePlayers()){
								completions.add(p.getName());
							}
						case "ability":
							for(Player p : Bukkit.getOnlinePlayers()){
								completions.add(p.getName());
							}
					}
				}
				else if(args[1].equalsIgnoreCase("replace")){
					if(Bukkit.getPlayer(args[2]) != null){
						McRPGPlayer mp = PlayerManager.getPlayer(Bukkit.getPlayer(args[2]).getUniqueId());
						for(UnlockedAbilities ab : mp.getAbilityLoadout()){
							completions.add(ab.getName());
						}
					}
				}
				else if(args[1].equalsIgnoreCase("remove")){
					McRPGPlayer mp = PlayerManager.getPlayer(Bukkit.getPlayer(args[2]).getUniqueId());
					for(UnlockedAbilities ab : mp.getAbilityLoadout()){
						completions.add(ab.getName());
					}
				}
				else if(args[1].equalsIgnoreCase("view")){
					if(Skills.isSkill(args[2]) || args[2].equalsIgnoreCase("loadout")){

					}
				}
			}
		}
		return completions;
	}
}
