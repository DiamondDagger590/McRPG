package us.eunoians.mcrpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.Skills;

public class McExp implements CommandExecutor{
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    McRPG plugin = McRPG.getInstance();
    FileConfiguration config = plugin.getLangFile();
    if(sender instanceof Player){
      Player p = (Player) sender;
      if(PlayerManager.isPlayerFrozen(p.getUniqueId())){
        return true;
      }
      if(args.length < 1){
        sendHelpMessage(p);
        return true;
      }
      else{
        if(!(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg.mcexp"))){
          p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
          return true;
        }
        if(!Skills.isSkill(args[0])){
          p.sendMessage(Methods.color(p, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
          return true;
        }
        else{
          Skills skill = Skills.fromString(args[0]);
          FileConfiguration skillFile = FileManager.Files.getSkillFile(skill).getFile();
          if(skill == Skills.SORCERY){
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&5Displaying " + skill.getDisplayName() + " Brew Exp Values"));
            for(String expVar : skillFile.getConfigurationSection("ExpAwardedPerBrewAmount").getKeys(false)){
              p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&7" + expVar + ": &f" + skillFile.getString("ExpAwardedPerBrewAmount." + expVar)));
            }
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&5Displaying " + skill.getDisplayName() + " Enchantment Exp Values"));
            for(String expVar : skillFile.getConfigurationSection("ExpAwardedPerEnchantment").getKeys(false)){
              p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&7" + expVar + ": &f" + skillFile.getString("ExpAwardedPerEnchantment." + expVar)));
            }
          }
          else if(skill == Skills.FISHING){
            skillFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FISHING_LOOT);
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&5Displaying " + skill.getDisplayName() + " Fishing Exp Values"));
            for(String cat : skillFile.getConfigurationSection("Categories").getKeys(false)){
              for(String item : skillFile.getConfigurationSection("Categories." + cat).getKeys(false)){
                p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&7" + item + ": &f" + skillFile.getString("Categories." + cat + "." + item + ".McRPGExp", "0")));
              }
            }
          }
          else{
            for(String configSection : skillFile.getConfigurationSection("").getKeys(false)){
              if(configSection.contains("Exp") && !configSection.contains("Equation")){
                p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&5Displaying " + skill.getDisplayName() + " Exp Values"));
                for(String expVar : skillFile.getConfigurationSection(configSection).getKeys(false)){
                  p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&7" + expVar + ": &f" + skillFile.getString(configSection + "." + expVar)));
                }
                break;
              }
            }
          }
          return true;
        }
      }
    }
    else{
      sender.sendMessage(Methods.color(plugin.getPluginPrefix()) + "&cConsole can not run this command.");
      return true;
    }
  }
  
  private void sendHelpMessage(CommandSender p){
    McRPG plugin = McRPG.getInstance();
    FileConfiguration config = plugin.getLangFile();
    p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.HelpPrompt").replaceAll("<command>", "mcparty")));
  }
}
