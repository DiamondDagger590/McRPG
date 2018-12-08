package us.eunoians.mcrpg.api.events.mcmmo;

import us.eunoians.mcrpg.abilities.mining.RicherOres;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class RicherOresEvent extends AbilityActivateEvent {

  public RicherOresEvent(McMMOPlayer player, RicherOres richerOres){
    super(richerOres, player);
  }
}
