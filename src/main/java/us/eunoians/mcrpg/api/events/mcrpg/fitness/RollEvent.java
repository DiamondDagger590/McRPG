package us.eunoians.mcrpg.api.events.mcrpg.fitness;

import us.eunoians.mcrpg.abilities.fitness.Roll;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class RollEvent extends AbilityActivateEvent {

  public RollEvent(McRPGPlayer mcRPGPlayer, Roll roll) {
    super(roll, mcRPGPlayer, AbilityEventType.RECREATIONAL);
  }
}
