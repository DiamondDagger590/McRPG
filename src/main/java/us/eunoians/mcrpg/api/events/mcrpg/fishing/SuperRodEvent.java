package us.eunoians.mcrpg.api.events.mcrpg.fishing;

import us.eunoians.mcrpg.abilities.fishing.SuperRod;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class SuperRodEvent extends AbilityActivateEvent {

  public SuperRodEvent(McRPGPlayer mcRPGPlayer, SuperRod superRod){
    super(superRod, mcRPGPlayer, AbilityEventType.RECREATIONAL);
  }
}
