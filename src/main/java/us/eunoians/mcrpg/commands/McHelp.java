package us.eunoians.mcrpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;

public class McHelp implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    McRPG plugin = McRPG.getInstance();
    FileConfiguration config = plugin.getLangFile();
    //Disabled Worlds
    if(sender instanceof Player) {
      Player p = (Player) sender;
      String world = p.getWorld().getName();
      if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
              McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world)) {
        return true;
      }
    }
    if(args.length == 0) {
      for(String prompt : config.getStringList("Messages.Commands.McHelp.Default")) {
        sender.sendMessage(Methods.color(prompt));
      }
      return true;
    }
    else {
      String help = args[0];
      if(help.equalsIgnoreCase("mcdisplay")) {
        for(String prompt : config.getStringList("Messages.Commands.McHelp.McDisplay")) {
          sender.sendMessage(Methods.color(prompt));
        }
        return true;
      }
      else if(help.equalsIgnoreCase("mcredeem")){
        for(String prompt : config.getStringList("Messages.Commands.McHelp.McRedeem")){
          sender.sendMessage(Methods.color(prompt));
        }
        return true;
      }
      else if(help.equalsIgnoreCase("mcexp")){
        for(String prompt : config.getStringList("Messages.Commands.McHelp.McExp")){
          sender.sendMessage(Methods.color(prompt));
        }
        return true;
      }
      else if(help.equalsIgnoreCase("mcadmin")) {
        String page = "1";
        if(args.length > 1) {
          page = args[1];
        }
        if(!(page.equalsIgnoreCase("4") || page.equalsIgnoreCase("2") || page.equalsIgnoreCase("3") || page.equalsIgnoreCase("1"))) {
          return true;
        }
        for(String prompt : config.getStringList("Messages.Commands.McHelp.McAdmin" + page)) {
          sender.sendMessage(Methods.color(prompt));
        }
        return true;
      }
      else if(help.equalsIgnoreCase("mcparty")) {
        String page = "1";
        if(args.length > 1) {
          page = args[1];
        }
        if(!(page.equalsIgnoreCase("4") || page.equalsIgnoreCase("2") || page.equalsIgnoreCase("3") || page.equalsIgnoreCase("1"))) {
          return true;
        }
        for(String prompt : config.getStringList("Messages.Commands.McHelp.McParty" + page)) {
          sender.sendMessage(Methods.color(prompt));
        }
        return true;
      }
      else {
        sender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Commands.McHelp.Error")));
        return true;
      }
    }
    //return false;
  }
}
