package us.eunoians.mcrpg.api.events.mcrpg.fishing;

import us.eunoians.mcrpg.abilities.fishing.SeaGodsBlessing;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class SeaGodsBlessingEvent extends AbilityActivateEvent {

  public SeaGodsBlessingEvent(McRPGPlayer mcRPGPlayer, SeaGodsBlessing seaGodsBlessing){
    super(seaGodsBlessing, mcRPGPlayer, AbilityEventType.RECREATIONAL);
  }
}
