package us.eunoians.mcrpg.api.events.mcmmo;

import us.eunoians.mcrpg.abilities.unarmed.StickyFingers;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class StickyFingersEvent extends AbilityActivateEvent {

  public StickyFingersEvent(McMMOPlayer player, StickyFingers stickyFingers){
    super(stickyFingers, player);
  }
}
