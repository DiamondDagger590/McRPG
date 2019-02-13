package us.eunoians.mcrpg.api.events.mcrpg.unarmed;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.unarmed.TighterGrip;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class TighterGripEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double gripBonus = 0.0;

  public TighterGripEvent(McRPGPlayer player, TighterGrip tighterGrip, double gripBonus){
    super(tighterGrip, player, AbilityEventType.COMBAT);
    this.gripBonus = gripBonus;
  }
}
