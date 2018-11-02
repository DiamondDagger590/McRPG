package us.eunoians.mcmmox.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.Methods;

public class McHelp implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	Mcmmox plugin = Mcmmox.getInstance();
	FileConfiguration config = plugin.getLangFile();
	if(args.length < 1){
	  return true;
	}
	else{
	  String help = args[0];
	  if(help.equalsIgnoreCase("mcdisplay")){
	    for(String prompt : config.getStringList("Messages.Commands.McHelp.McDisplay")){
	      sender.sendMessage(Methods.color(prompt));
		}
		return true;
	  }
	  else if(help.equalsIgnoreCase("mcadmin")){
		String page = "1";
		if(args.length > 1){
		  page = args[1];
		}
		if(!(page.equalsIgnoreCase("2") || page.equalsIgnoreCase("3") || page.equalsIgnoreCase("1"))){
		  return true;
		}
		for(String prompt : config.getStringList("Messages.Commands.McHelp.McAdmin" + page)){
		  sender.sendMessage(Methods.color(prompt));
		}
		return true;
	  }
	  else{
		return true;
	  }
	}
	//return false;
  }
}
