package us.eunoians.mcrpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.gui.FileGUI;
import us.eunoians.mcrpg.gui.GUI;
import us.eunoians.mcrpg.gui.GUITracker;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.HardCodedGuis;
import us.eunoians.mcrpg.types.Skills;

import java.io.File;

public class GUIOpenCommand implements CommandExecutor{
  
  // /mcgui opennative
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    if(sender instanceof Player){
      Player player = (Player) sender;
      McRPGPlayer mcRPGPlayer;
      try{
        mcRPGPlayer = PlayerManager.getPlayer(player.getUniqueId());
      }catch(McRPGPlayerNotFoundException e){
        return true;
      }
      if(args.length < 2){
        player.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() + "&cInvalid command"));
        return true;
      }
      else{
        if(args[0].equalsIgnoreCase("OpenNative")){
          HardCodedGuis hardCodedGui = HardCodedGuis.fromID(args[1]);
          if(hardCodedGui == null){
            player.closeInventory();
            return true;
          }
          else if(hardCodedGui.isAcceptSkill()){
            if(args.length == 2 || !Skills.isSkill(args[2])){
              player.closeInventory();
              return true;
            }
            else{
              GUI newGUI = hardCodedGui.buildGUI(mcRPGPlayer, Skills.fromString(args[2]));
              if(GUITracker.isPlayerTracked(mcRPGPlayer)){
                GUI oldGUI = GUITracker.getPlayersGUI(mcRPGPlayer);
                oldGUI.setClearData(false);
                player.openInventory(newGUI.getGui().getInv());
                GUITracker.replacePlayersGUI(mcRPGPlayer, newGUI);
                return true;
              }
              else{
                player.openInventory(newGUI.getGui().getInv());
                GUITracker.trackPlayer(mcRPGPlayer, newGUI);
                return true;
              }
            }
          }
          else{
            GUI newGUI = hardCodedGui.buildGUI(mcRPGPlayer, null);
            if(GUITracker.isPlayerTracked(mcRPGPlayer)){
              GUI oldGUI = GUITracker.getPlayersGUI(mcRPGPlayer);
              oldGUI.setClearData(false);
              player.openInventory(newGUI.getGui().getInv());
              GUITracker.replacePlayersGUI(mcRPGPlayer, newGUI);
              return true;
            }
            else{
              player.openInventory(newGUI.getGui().getInv());
              GUITracker.trackPlayer(mcRPGPlayer, newGUI);
              return true;
            }
          }
        }
        File file = new File(McRPG.getInstance().getDataFolder(), args[0]);
        File file2 = new File(McRPG.getInstance().getDataFolder(), File.separator + "guis" + File.separator + args[0]);
        if(file.exists()){
          FileGUI fileGUI = new FileGUI(mcRPGPlayer, file, args[1]);
          if(GUITracker.isPlayerTracked(mcRPGPlayer)){
            GUI oldGUI = GUITracker.getPlayersGUI(mcRPGPlayer);
            oldGUI.setClearData(false);
            player.openInventory(fileGUI.getGui().getInv());
            GUITracker.replacePlayersGUI(mcRPGPlayer, fileGUI);
            return true;
          }
          else{
            player.openInventory(fileGUI.getGui().getInv());
            GUITracker.trackPlayer(mcRPGPlayer, fileGUI);
            return true;
          }
        }
        else if(file2.exists()){
          FileGUI fileGUI = new FileGUI(mcRPGPlayer, file2, args[1]);
          if(GUITracker.isPlayerTracked(mcRPGPlayer)){
            GUI oldGUI = GUITracker.getPlayersGUI(mcRPGPlayer);
            oldGUI.setClearData(false);
            player.openInventory(fileGUI.getGui().getInv());
            GUITracker.replacePlayersGUI(mcRPGPlayer, fileGUI);
            return true;
          }
          else{
            player.openInventory(fileGUI.getGui().getInv());
            GUITracker.trackPlayer(mcRPGPlayer, fileGUI);
            return true;
          }
        }
        else{
          sender.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() + "&cThe file specified does not exist"));
          return true;
        }
      }
    }
    else{
      sender.sendMessage("Only players can run this command");
      return true;
    }
  }
}
