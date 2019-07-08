package us.eunoians.mcrpg.api.events.mcrpg.fishing;

import us.eunoians.mcrpg.abilities.fishing.GreatRod;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class GreatRodEvent extends AbilityActivateEvent {

  public GreatRodEvent(McRPGPlayer mcRPGPlayer, GreatRod greatRod){
    super(greatRod, mcRPGPlayer, AbilityEventType.RECREATIONAL);
  }
}
