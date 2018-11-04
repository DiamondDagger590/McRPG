package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.abilities.unarmed.DenseImpact;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class DenseImpactEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int armourDmg;

  public DenseImpactEvent(McMMOPlayer player, DenseImpact denseImpact, int armourDmg){
    super(denseImpact, player);
    this.armourDmg = armourDmg;
  }
}
