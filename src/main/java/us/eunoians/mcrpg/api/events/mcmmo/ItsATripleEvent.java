package us.eunoians.mcrpg.api.events.mcmmo;

import us.eunoians.mcrpg.abilities.mining.ItsATriple;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class ItsATripleEvent extends AbilityActivateEvent {

  public ItsATripleEvent(McMMOPlayer player, ItsATriple itsATriple){
    super(itsATriple, player);
  }
}
