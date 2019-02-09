package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.herbalism.PansBlessing;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class PansBlessingEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int radius;

  public PansBlessingEvent(McRPGPlayer player, PansBlessing pansBlessing, int radius){
    super(pansBlessing, player, AbilityEventType.RECREATIONAL);
    this.radius = radius;
  }
}
