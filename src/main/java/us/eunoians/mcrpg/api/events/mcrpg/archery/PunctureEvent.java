package us.eunoians.mcrpg.api.events.mcrpg.archery;

import us.eunoians.mcrpg.abilities.archery.Puncture;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class PunctureEvent extends AbilityActivateEvent {

  public PunctureEvent(McRPGPlayer mcRPGPlayer, Puncture puncture){
    super(puncture, mcRPGPlayer, AbilityEventType.COMBAT);
  }
}
