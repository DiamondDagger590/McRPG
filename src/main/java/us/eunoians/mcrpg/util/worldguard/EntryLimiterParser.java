package us.eunoians.mcrpg.util.worldguard;

import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * Handles evaluating an equation for a single player
 *
 * @author DiamondDagger590
 */
public class EntryLimiterParser extends McRPGParser {

    /**
     * Evaluates the provided expression for the provided player
     *
     * @param player     The {@link McRPGPlayer} that is being checked
     * @param expression A {@link String} expression to be evaluated
     * @return {@code true} if the expression passes
     */
    //TODO
    public boolean evaluateExpression(McRPGPlayer player, String expression) {
        String[] info = expression.split(" ");
        int var = Integer.parseInt(info[2]);
        boolean result = false;
        if (info[0].equalsIgnoreCase("power_level")) {
            //result = evaluate(info[1], player.getPowerLevel(), var);
        } else if (info[0].contains("skill_level")) {
            String s = info[0].replace("skill_level(", "").replace(")", "");
            //result = evaluate(info[1], player.getSkill(s).getCurrentLevel(), var);
        }
        return result;
    }
}
