package us.eunoians.mcrpg.abilities.fitness;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class BulletProof extends BaseAbility {

  public BulletProof(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.BULLET_PROOF, isToggled, currentTier);
  }
}
