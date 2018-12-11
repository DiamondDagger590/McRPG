package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.unarmed.IronArm;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class IronArmEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int bonusDamage;

  public IronArmEvent(McRPGPlayer player, IronArm ironArm, int bonusDamage){
    super(ironArm, player);
    this.bonusDamage = bonusDamage;
  }
}
