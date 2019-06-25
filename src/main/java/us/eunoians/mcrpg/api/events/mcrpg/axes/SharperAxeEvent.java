package us.eunoians.mcrpg.api.events.mcrpg.axes;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.axes.SharperAxe;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class SharperAxeEvent extends AbilityActivateEvent {

  @Getter @Setter private int lowBound;
  @Getter @Setter private int highBound;

  public SharperAxeEvent(McRPGPlayer player, SharperAxe sharperAxe, int lowBound, int highBound){
    super(sharperAxe, player, AbilityEventType.COMBAT);
    this.highBound = highBound;
    this.lowBound = lowBound;
  }
}
