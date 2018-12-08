package us.eunoians.mcrpg.api.events.mcmmo;

import us.eunoians.mcrpg.abilities.mining.DoubleDrop;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class DoubleDropEvent extends AbilityActivateEvent {

  public DoubleDropEvent(McMMOPlayer player, DoubleDrop drop){
    super(drop, player);
  }
}
