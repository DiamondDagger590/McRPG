package us.eunoians.mcrpg.util.worldguard;

import org.bukkit.Bukkit;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class EntryLimiterParser extends McRPGParser {

  public boolean evaluateExpression(McRPGPlayer player, String expression) {
    String[] info = expression.split(" ");
    if (info.length < 3) {
      Bukkit.getLogger().warning("[McRPG] Invalid BanEntry expression: '" + expression + "' - expected format: '<condition> <operator> <value>'");
      return false;
    }
    int var = Integer.parseInt(info[2]);
    boolean result = false;
    if(info[0].equalsIgnoreCase("power_level")){
      result = evaluate(info[1], player.getPowerLevel(), var);
    }
    else if(info[0].contains("skill_level")){
      String s = info[0].replace("skill_level(", "").replace(")", "");
      result = evaluate(info[1], player.getSkill(s).getCurrentLevel(), var);
    }
    return result;
  }
}
