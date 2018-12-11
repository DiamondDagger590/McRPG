package us.eunoians.mcrpg.api.events.mcrpg;

import us.eunoians.mcrpg.abilities.mining.ItsATriple;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class ItsATripleEvent extends AbilityActivateEvent {

  public ItsATripleEvent(McRPGPlayer player, ItsATriple itsATriple){
    super(itsATriple, player);
  }
}
