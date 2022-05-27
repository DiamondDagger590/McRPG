package us.eunoians.mcrpg.worldguard;

import lombok.Getter;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class ActionLimiterParser extends McRPGParser {

  @Getter
  private String equation;

  @Getter
  private McRPGPlayer[] players;

  /**
   * @param equation The equation from the config
   * @param players  At least one player but a second is optional for comparisons
   */
  public ActionLimiterParser(String equation, McRPGPlayer... players) {
    this.equation = equation;
    this.players = players;
  }

  /**
   * Evaluate the various parameters supported
   *
   * @return Result of the expression
   */
  public boolean evaluateExpression() {
    boolean result = false;
    McRPGPlayer player = players[0];
    String[] expression = equation.split(" ");
    if(Skills.isSkill(expression[0])) {
      int level = player.getSkill(expression[0]).getCurrentLevel();
      int var = Integer.parseInt(expression[2]);
      result = evaluate(expression[1], level, var);
    }
    else if(expression[0].equals("power_level")) {
      int level = player.getPowerLevel();
      int var = Integer.parseInt(expression[2]);
      result = evaluate(expression[1], level, var);
    }
    else if(expression[0].equals("power_level_difference")) {
      McRPGPlayer target = players[1];
      int diff = player.getPowerLevel() - target.getPowerLevel();
      int var = Integer.parseInt(expression[2]);
      result = evaluate(expression[1], diff, var);
    }
    else if(expression[0].contains("skill_difference")) {
      McRPGPlayer target = players[1];
      String s = expression[0].replace("skill_difference(", "").replace(")", "");
      int diff = player.getSkill(s).getCurrentLevel() - target.getSkill(s).getCurrentLevel();
      int var = Integer.parseInt(expression[2]);
      result = evaluate(expression[1], diff, var);
    }
    else if(expression[0].contains("skill_level")) {
      String s = expression[0].replace("skill_level(", "").replace(")", "");
      int level = player.getSkill(s).getCurrentLevel();
      int var = Integer.parseInt(expression[2]);
      result = evaluate(expression[1], level, var);
    }
    else if(expression[0].contains("ability_tier")) {
      String a = expression[0].replace("ability_tier(", "").replace("}", "");
      int tier = player.getBaseAbility(UnlockedAbilities.fromString(a)).getCurrentTier();
      int var = Integer.parseInt(expression[2]);
      result = evaluate(expression[1], tier, var);
    }
    return result;
  }

  /**
   * Used with the ability_tier(self) parameter
   *
   * @param ability Ability being used
   * @return If the condition is met
   */
  public boolean evaluateExpression(UnlockedAbilities ability) {
    boolean result = false;
    McRPGPlayer player = players[0];
    String[] expression = equation.split(" ");
    if(expression[0].contains("ability_tier") && expression[0].contains("self")) {
      result = evaluate(expression[1], player.getBaseAbility(ability).getCurrentTier(), Integer.parseInt(expression[2]));
    }
    return result;
  }

  public boolean evaluateExpression(Skills s) {
    boolean result = false;
    McRPGPlayer player = players[0];
    String[] expression = equation.split(" ");
    if(expression[0].contains("skill_level") && expression[0].contains("self")) {
      result = evaluate(expression[1], player.getSkill(s).getCurrentLevel(), Integer.parseInt(expression[2]));
    }
    return result;
  }
}
