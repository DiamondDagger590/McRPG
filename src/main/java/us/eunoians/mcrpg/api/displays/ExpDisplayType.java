package us.eunoians.mcrpg.api.displays;

import us.eunoians.mcrpg.types.Skills;

public interface ExpDisplayType {

  /**
   * A method all children must have. Updates display w/ new info
   * @param currentExp
   * @param expToLevel
   * @param currentLevel
   */
  void sendUpdate(int currentExp, int expToLevel, int currentLevel, int expGained);

  Skills getSkill();
}
