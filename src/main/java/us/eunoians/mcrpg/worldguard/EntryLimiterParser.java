package us.eunoians.mcrpg.worldguard;

import us.eunoians.mcrpg.players.McRPGPlayer;

public class EntryLimiterParser extends McRPGParser {

  public boolean evaluateExpression(McRPGPlayer player, String expression) {
    String[] info = expression.split(" ");
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
