package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;

public class McRPGPlayerLevelChangeEvent extends PlayerModifiedEvent {

  @Getter
  private int previousLevel;
  @Getter
  private int nextLevel;
  @Getter
  private int amountOfLevelsIncreased;
  @Getter
  private Skill skillLeveled;

  public McRPGPlayerLevelChangeEvent(McRPGPlayer mcRPGPlayer, int previousLevel, int nextLevel, int amountOfLevelsIncreased, Skill skillLeveled) {
    super(mcRPGPlayer);
    this.previousLevel = previousLevel;
    this.nextLevel = nextLevel;
    this.amountOfLevelsIncreased = amountOfLevelsIncreased;
    this.skillLeveled = skillLeveled;
  }

  public McRPGPlayer getMcMMOPlayer() {
    return skillLeveled.getPlayer();
  }
}