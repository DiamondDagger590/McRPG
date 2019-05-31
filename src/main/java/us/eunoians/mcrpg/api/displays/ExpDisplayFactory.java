package us.eunoians.mcrpg.api.displays;

import org.bukkit.scoreboard.Scoreboard;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.types.Skills;

public class ExpDisplayFactory {

  public static GenericDisplay createDisplay(DisplayType displayType, McRPGPlayer player, Skills skill) {
    if(displayType == DisplayType.ACTION_BAR) {
      return new ExpActionBar(player, skill);
    }
    else if(displayType == DisplayType.BOSS_BAR) {
      return new ExpBossbarDisplay(player, skill);
    }
    else if(displayType == DisplayType.SCOREBOARD) {
      return new ExpScoreboardDisplay(player, skill, player.getPlayer().getScoreboard());
    }
    else {
      return null;
    }
  }

  public static GenericDisplay createDisplay(DisplayType displayType, McRPGPlayer player, Skills skill, int displayTime) {
    if(displayType == DisplayType.ACTION_BAR) {
      return new DecayableExpActionBar(player, skill);
    }
    else if(displayType == DisplayType.BOSS_BAR) {
      return new DecayableExpBossBar(player, skill, displayTime);
    }
    else if(displayType == DisplayType.SCOREBOARD) {
      return new DecayableExpScoreboard(player, skill, player.getPlayer().getScoreboard(), displayTime);
    }
    else {
      return null;
    }
  }

  public static GenericDisplay createDisplay(McRPGPlayer player, Skills skill, Scoreboard old) {
    return new ExpScoreboardDisplay(player, skill, old);
  }

  public static GenericDisplay createDisplay(McRPGPlayer player, Skills skill, Scoreboard old, int displayTime) {
    return new DecayableExpScoreboard(player, skill, old, displayTime);
  }
}
