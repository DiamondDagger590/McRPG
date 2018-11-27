package us.eunoians.mcmmox.api.events.mcmmo;

import us.eunoians.mcmmox.abilities.mining.DoubleDrop;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class DoubleDropEvent extends AbilityActivateEvent {

  public DoubleDropEvent(McMMOPlayer player, DoubleDrop drop){
    super(drop, player);
  }
}
