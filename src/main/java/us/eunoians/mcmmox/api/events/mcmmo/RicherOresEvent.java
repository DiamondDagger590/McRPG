package us.eunoians.mcmmox.api.events.mcmmo;

import us.eunoians.mcmmox.abilities.mining.RicherOres;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class RicherOresEvent extends AbilityActivateEvent {

  public RicherOresEvent(McMMOPlayer player, RicherOres richerOres){
    super(richerOres, player);
  }
}
