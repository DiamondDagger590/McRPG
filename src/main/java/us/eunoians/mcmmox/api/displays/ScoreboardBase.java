package us.eunoians.mcmmox.api.displays;

import org.bukkit.scoreboard.Scoreboard;

public interface ScoreboardBase {

  Scoreboard getOldScoreBoard();

  boolean hasOldScoreBoard();
}
