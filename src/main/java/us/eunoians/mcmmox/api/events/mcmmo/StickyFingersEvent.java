package us.eunoians.mcmmox.api.events.mcmmo;

import us.eunoians.mcmmox.abilities.unarmed.StickyFingers;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class StickyFingersEvent extends AbilityActivateEvent {

  public StickyFingersEvent(McMMOPlayer player, StickyFingers stickyFingers){
    super(stickyFingers, player);
  }
}
