package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.abilities.unarmed.TighterGrip;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class TighterGripEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double gripBonus = 0.0;

  public TighterGripEvent(McMMOPlayer player, TighterGrip tighterGrip, double gripBonus){
    super(tighterGrip, player);
    this.gripBonus = gripBonus;
  }
}
