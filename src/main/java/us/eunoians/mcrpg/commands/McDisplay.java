package us.eunoians.mcrpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.displays.DisplayManager;
import us.eunoians.mcrpg.api.displays.ExpActionBar;
import us.eunoians.mcrpg.api.displays.ExpBossbarDisplay;
import us.eunoians.mcrpg.api.displays.ExpScoreboardDisplay;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.types.Skills;

public class McDisplay implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    McRPG plugin = McRPG.getInstance();
    DisplayManager displayManager = plugin.getDisplayManager();
    FileConfiguration config = plugin.getLangFile();
    if(sender instanceof Player) {
      Player p = (Player) sender;
      if(PlayerManager.isPlayerFrozen(p.getUniqueId())){
        return true;
      }
      String world = p.getWorld().getName();
      //Disabled Worlds
      if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
              McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world)) {
        return true;
      }

      if(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg.display")) {
        if(args.length == 0) {
          p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.HelpPrompt").replaceAll("<command>", "mcdisplay")));
          return true;
        }
        if(args.length == 1) {
          // /mcdisplay clear
          if(args[0].equalsIgnoreCase("clear")) {
            if(displayManager.doesPlayerHaveDisplay(p)) {
              displayManager.removePlayersDisplay(p);
              return true;
            }
            else {
              p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.NothingToClear")));
              return true;
            }
          }
          // /mcdisplay keep
          else if(args[0].equalsIgnoreCase("keep")) {
            //TODO
            return true;
          }
          // /mcdisplay {skill}
          else if(Skills.isSkill(args[0])) {
            McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
            if(DisplayManager.getInstance().doesPlayerHaveDisplay(p)) {
              DisplayManager.getInstance().getDisplay(p).cancel();
              DisplayManager.getInstance().removePlayersDisplay(p);
            }
            DisplayType type = mp.getDisplayType();
            return setDisplay(args, displayManager, p, mp, type);
          }
          // not those
          else {
            p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.InvalidInput").replaceAll("%String%", args[0])));
            return true;
          }
        }
        else {
          // /mcdisplay set {displaytype}
          if(args[0].equalsIgnoreCase("set")) {
            if(DisplayType.isDisplayType(args[1])) {
              DisplayType type = DisplayType.fromString(args[1]);
              McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
              mp.setDisplayType(type);
              p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.DisplayChanged").replaceAll("%DisplayType%", type.getName())));
              return true;
            }
            else {
              p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Commands.McDisplay.InvalidInput").replaceAll("%String%", args[0])));
              return true;
            }
          }
          // /mcdisplay {skill} {displaytype}
          else if(Skills.isSkill(args[0])) {
            if(DisplayType.isDisplayType(args[1])) {
              McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
              DisplayType type = DisplayType.fromString(args[1]);
              return setDisplay(args, displayManager, p, mp, type);
            }
            else {
              p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.InvalidInput").replaceAll("%String%", args[0])));
              return true;
            }
          }
          // Not that hard to do it right yall.
          else {
            p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.McDisplay.InvalidInput").replaceAll("%String%", args[0])));
            return true;
          }
        }
      }
      else {
        p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
        return true;
      }
    }
    else {
      sender.sendMessage(Methods.color(plugin.getPluginPrefix()) + "&cConsole can not run this command.");
      return true;
    }
  }

  private boolean setDisplay(String[] args, DisplayManager displayManager, Player p, McRPGPlayer mp, DisplayType type) {
    if(type.equals(DisplayType.SCOREBOARD)) {
      ExpScoreboardDisplay display = new ExpScoreboardDisplay(mp, Skills.fromString(args[0]), p.getScoreboard());
      p.setScoreboard(display.getBoard());
      displayManager.setGenericDisplay(display);
      return true;
    }
    else if(type.equals(DisplayType.BOSS_BAR)) {
      ExpBossbarDisplay display = new ExpBossbarDisplay(mp, Skills.fromString(args[0]));
      displayManager.setGenericDisplay(display);
      return true;
    }
    else {
      ExpActionBar display = new ExpActionBar(mp, Skills.fromString(args[0]));
      displayManager.setGenericDisplay(display);
      return true;
    }
  }
}
