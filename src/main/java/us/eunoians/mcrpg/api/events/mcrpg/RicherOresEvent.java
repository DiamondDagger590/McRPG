package us.eunoians.mcrpg.api.events.mcrpg;

import us.eunoians.mcrpg.abilities.mining.RicherOres;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class RicherOresEvent extends AbilityActivateEvent {

  public RicherOresEvent(McRPGPlayer player, RicherOres richerOres){
    super(richerOres, player, AbilityEventType.RECREATIONAL);
  }
}
