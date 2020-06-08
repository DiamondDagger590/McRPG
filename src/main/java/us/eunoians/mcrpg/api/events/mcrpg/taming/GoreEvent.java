package us.eunoians.mcrpg.api.events.mcrpg.taming;

import us.eunoians.mcrpg.abilities.taming.Gore;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class GoreEvent extends AbilityActivateEvent{
  
  public GoreEvent(McRPGPlayer mcRPGPlayer, Gore gore){
    super(gore, mcRPGPlayer, AbilityEventType.COMBAT);
  }
}
