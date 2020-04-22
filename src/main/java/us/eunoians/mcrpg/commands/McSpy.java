package us.eunoians.mcrpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;

public class McSpy implements CommandExecutor{

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    if(sender instanceof Player){
      Player p = (Player) sender;
      if(PlayerManager.isPlayerFrozen(p.getUniqueId())){
        return true;
      }
      if(!(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg.mcspy"))){
        p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Utility.NoPerms")));
        return true;
      }
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(p.getUniqueId());
      }
      catch(McRPGPlayerNotFoundException exception){
        return true;
      }
      mp.setSpyPartyChat(!mp.isSpyPartyChat());
      if(mp.isSpyPartyChat()){
        p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&aYou have enabled party chat spying!"));
      }
      else{
        p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&cYou have disabled party chat spying!"));
      }
      return true;
    }
    else{
      sender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix()) + "&cConsole can not run this command.");
      return true;
    }
  }
}
