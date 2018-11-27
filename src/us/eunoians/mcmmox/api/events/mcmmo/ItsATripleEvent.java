package us.eunoians.mcmmox.api.events.mcmmo;

import us.eunoians.mcmmox.abilities.mining.ItsATriple;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class ItsATripleEvent extends AbilityActivateEvent {

  public ItsATripleEvent(McMMOPlayer player, ItsATriple itsATriple){
    super(itsATriple, player);
  }
}
