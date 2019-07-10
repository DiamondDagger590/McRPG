package us.eunoians.mcrpg.api.events.mcrpg.fishing;

import us.eunoians.mcrpg.abilities.fishing.SunkenArmory;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class SunkenArmoryEvent extends AbilityActivateEvent {

  public SunkenArmoryEvent(McRPGPlayer mcRPGPlayer, SunkenArmory sunkenArmory){

    super(sunkenArmory, mcRPGPlayer, AbilityEventType.RECREATIONAL);
  }
}
