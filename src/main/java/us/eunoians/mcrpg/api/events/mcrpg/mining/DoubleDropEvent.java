package us.eunoians.mcrpg.api.events.mcrpg.mining;

import us.eunoians.mcrpg.abilities.mining.DoubleDrop;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class DoubleDropEvent extends AbilityActivateEvent {

  public DoubleDropEvent(McRPGPlayer player, DoubleDrop drop){
    super(drop, player, AbilityEventType.RECREATIONAL);
  }
}
