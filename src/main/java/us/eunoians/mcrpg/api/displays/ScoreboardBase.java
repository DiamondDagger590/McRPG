package us.eunoians.mcrpg.api.displays;

import org.bukkit.scoreboard.Scoreboard;

public interface ScoreboardBase {

  Scoreboard getOldScoreBoard();

  boolean hasOldScoreBoard();
}
