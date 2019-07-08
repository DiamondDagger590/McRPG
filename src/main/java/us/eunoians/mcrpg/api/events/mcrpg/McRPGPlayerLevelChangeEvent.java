package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;

public class McRPGPlayerLevelChangeEvent extends PlayerModifiedEvent {

  @Getter
  private int previousLevel;
  @Getter @Setter
  private int nextLevel;
  @Getter
  private Skill skillLeveled;

  public McRPGPlayerLevelChangeEvent(McRPGPlayer mcRPGPlayer, int previousLevel, int nextLevel, Skill skillLeveled) {
    super(mcRPGPlayer);
    this.previousLevel = previousLevel;
    this.nextLevel = nextLevel;
    this.skillLeveled = skillLeveled;
  }

  public McRPGPlayer getMcRPGPlayer() {
    return skillLeveled.getPlayer();
  }

  public int getAmountOfLevelsIncreased(){
    return nextLevel-previousLevel;
  }
}