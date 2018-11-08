package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.abilities.herbalism.PansBlessing;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class PansBlessingEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int radius;

  public PansBlessingEvent(McMMOPlayer player, PansBlessing pansBlessing, int radius){
    super(pansBlessing, player);
    this.radius = radius;
  }
}
