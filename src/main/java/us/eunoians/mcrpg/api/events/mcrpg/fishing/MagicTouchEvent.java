package us.eunoians.mcrpg.api.events.mcrpg.fishing;

import us.eunoians.mcrpg.abilities.fishing.MagicTouch;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class MagicTouchEvent extends AbilityActivateEvent {

  public MagicTouchEvent(McRPGPlayer mcRPGPlayer, MagicTouch magicTouch){
    super(magicTouch, mcRPGPlayer, AbilityEventType.RECREATIONAL);
  }
}
