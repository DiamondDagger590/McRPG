package us.eunoians.mcmmox.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.displays.DisplayManager;
import us.eunoians.mcmmox.api.displays.ExpScoreboardDisplay;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.types.DisplayType;
import us.eunoians.mcmmox.types.Skills;

public class McDisplay implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	Mcmmox plugin = Mcmmox.getInstance();
	DisplayManager displayManager = plugin.getDisplayManager();
	FileConfiguration config = plugin.getLangFile();
	if(sender instanceof Player){
	  Player p = (Player) sender;
	  if(p.hasPermission("mcmmo.*") || p.hasPermission("mcmmo.display")){
		if(args.length == 0){
		  p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.HelpPrompt").replaceAll("<command>", "mcdisplay")));
		  return true;
		}
		if(args.length == 1){
		  // /mcdisplay clear
		  if(args[0].equalsIgnoreCase("clear")){
			if(displayManager.doesPlayerHaveDisplay(p)){
			  displayManager.removePlayersDisplay(p);
			  return true;
			}
			else{
			  p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.NothingToClear")));
			  return true;
			}
		  }
		  // /mcdisplay keep
		  else if(args[0].equalsIgnoreCase("keep")){
		    //TODO
		  }
		  // /mcdisplay {skill}
		  else if(Skills.isSkill(args[0])){
			McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
			DisplayType type = mp.getDisplayType();
			if(type.equals(DisplayType.EXP_SCOREBOARD)){
			  ExpScoreboardDisplay display = new ExpScoreboardDisplay(mp, Skills.fromString(args[0]), p.getScoreboard());
			  p.setScoreboard(display.getBoard());
			  displayManager.setGenericDisplay(display);
			  return true;
			}
			else{
			  //TODO
			  p.sendMessage("not added");
			  return true;
			}
		  }
		  // not those
		  else{
			p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.InvalidInput").replaceAll("%String%", args[0])));
			return true;
		  }
		}
		else{
		  // /mcdisplay set {displaytype}
		  if(args[0].equalsIgnoreCase("set")){
		    if(DisplayType.isDisplayType(args[1])){
		      DisplayType type = DisplayType.fromString(args[1]);
		      McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
		      mp.setDisplayType(type);
		      p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.DisplayChanged").replaceAll("%DisplayType%", type.getName())));
		      return true;
		    }
			else{
			  p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Commands.McDisplay.InvalidInput").replaceAll("%String%", args[0])));
			  return true;
			}
		  }
		  // /mcdisplay {skill} {displaytype}
		  else if(Skills.isSkill(args[0])){
			if(DisplayType.isDisplayType(args[1])){

			}
			else{
			  p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.InvalidInput").replaceAll("%String%", args[0])));
			  return true;
			}
		  }
		  // Not that hard to do it right yall.
		  else{
			p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.InvalidInput").replaceAll("%String%", args[0])));
			return true;
		  }
		}
	  }
	  else{
	    p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
	    return true;
	  }
	  return true;
	}
	else{
	  sender.sendMessage(Methods.color(plugin.getPluginPrefix()) + "&cConsole can not run this command.");
	  return true;
	}
  }
}
