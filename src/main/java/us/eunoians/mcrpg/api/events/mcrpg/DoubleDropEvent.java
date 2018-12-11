package us.eunoians.mcrpg.api.events.mcrpg;

import us.eunoians.mcrpg.abilities.mining.DoubleDrop;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class DoubleDropEvent extends AbilityActivateEvent {

  public DoubleDropEvent(McRPGPlayer player, DoubleDrop drop){
    super(drop, player);
  }
}
