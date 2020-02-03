package us.eunoians.mcrpg.commands;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.util.Parser;

import java.util.logging.Level;

public class McConvert implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(sender instanceof Player){
      return true;
    }
    else{
      if(McRPG.getInstance().isMcmmoEnabled()){
        Bukkit.getLogger().log(Level.INFO, Methods.color(McRPG.getInstance().getPluginPrefix() + "&eBeginning McMMO conversion... please hold."));
        Parser equation = new Parser(McRPG.getInstance().getConfig().getString("Configuration.McMMOConversionEquation"));
        int playersConverted = 0;
        a: for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()){
          if(playersConverted != 0 && playersConverted % 5 == 0){
            Bukkit.getLogger().log(Level.INFO, Methods.color(McRPG.getInstance().getPluginPrefix() + "&eConverted " + playersConverted + " players so far..."));
          }
          McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
          int expToConvert = 0;
          for(PrimarySkillType skillType : PrimarySkillType.NON_CHILD_SKILLS){
            try{
              expToConvert += mcMMO.getFormulaManager().calculateTotalExperience(ExperienceAPI.getLevelOffline(offlinePlayer.getUniqueId(), skillType.getName()),
                ExperienceAPI.getOfflineXP(offlinePlayer.getUniqueId(), skillType.getName()));
            }
            catch(Exception e){
              Bukkit.getLogger().log(Level.INFO, Methods.color(McRPG.getInstance().getPluginPrefix() + "&eUnable to find data to convert for player " + offlinePlayer.getName() + "."));
              continue a;
            }
          }
          if(expToConvert == 0){
            Bukkit.getLogger().log(Level.INFO, Methods.color(McRPG.getInstance().getPluginPrefix() + "&eUnable to find data to convert for player " + offlinePlayer.getName() + "."));
            continue;
          }
          equation.setVariable("skill_exp", expToConvert);
          mp.setBoostedExp((int) equation.getValue());
          mp.saveData();
          playersConverted++;
        }
        Bukkit.getLogger().log(Level.INFO, Methods.color(McRPG.getInstance().getPluginPrefix() + "&aFinished converting " + playersConverted + " players to McRPG. Please keep a copy of your mcmmo files in case you want to change back! :)" +
                                                           " Thank you for running McRPG. Remember to shut down server and remove mcMMO for this to function properly!"));
      }
      else{
        sender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&cMcMMO was not found on the server and so data could not be converted"));
      }
      //Legacy commandMcRPG.getInstance().getMcRPGDb().convertLegacyToFlatDB();
      return true;
    }
  }
}
