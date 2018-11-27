package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.abilities.unarmed.IronArm;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class IronArmEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int bonusDamage;

  public IronArmEvent(McMMOPlayer player, IronArm ironArm, int bonusDamage){
    super(ironArm, player);
    this.bonusDamage = bonusDamage;
  }
}
