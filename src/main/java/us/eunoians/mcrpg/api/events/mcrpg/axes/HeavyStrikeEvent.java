package us.eunoians.mcrpg.api.events.mcrpg.axes;

import us.eunoians.mcrpg.abilities.axes.HeavyStrike;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class HeavyStrikeEvent extends AbilityActivateEvent {

  public HeavyStrikeEvent(McRPGPlayer player, HeavyStrike heavyStrike){
    super(heavyStrike, player, AbilityEventType.COMBAT);
  }
}
