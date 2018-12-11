package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.unarmed.TighterGrip;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class TighterGripEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double gripBonus = 0.0;

  public TighterGripEvent(McRPGPlayer player, TighterGrip tighterGrip, double gripBonus){
    super(tighterGrip, player);
    this.gripBonus = gripBonus;
  }
}
