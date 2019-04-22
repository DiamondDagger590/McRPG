package us.eunoians.mcrpg.api.events.mcrpg.woodcutting;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.woodcutting.NymphsVitality;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class NymphsVitalityEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int newHunger;

  @Getter
  private int prevHunger;

  public NymphsVitalityEvent(McRPGPlayer player, NymphsVitality nymphsVitality, int newHunger, int prevHunger){
    super(nymphsVitality, player, AbilityEventType.RECREATIONAL);
    this.newHunger = newHunger;
    this.prevHunger = prevHunger;
  }
}
