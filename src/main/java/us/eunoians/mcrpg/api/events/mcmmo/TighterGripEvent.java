package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.unarmed.TighterGrip;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class TighterGripEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double gripBonus = 0.0;

  public TighterGripEvent(McMMOPlayer player, TighterGrip tighterGrip, double gripBonus){
    super(tighterGrip, player);
    this.gripBonus = gripBonus;
  }
}
