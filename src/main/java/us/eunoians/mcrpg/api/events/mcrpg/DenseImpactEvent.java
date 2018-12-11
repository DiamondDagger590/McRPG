package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.unarmed.DenseImpact;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class DenseImpactEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int armourDmg;

  public DenseImpactEvent(McRPGPlayer player, DenseImpact denseImpact, int armourDmg){
    super(denseImpact, player);
    this.armourDmg = armourDmg;
  }
}
