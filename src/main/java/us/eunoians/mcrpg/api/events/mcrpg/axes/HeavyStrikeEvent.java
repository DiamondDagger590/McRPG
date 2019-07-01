package us.eunoians.mcrpg.api.events.mcrpg.axes;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.axes.HeavyStrike;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class HeavyStrikeEvent extends AbilityActivateEvent {

  @Getter @Setter private double bonusChance;

  public HeavyStrikeEvent(McRPGPlayer player, HeavyStrike heavyStrike, double bonus){
    super(heavyStrike, player, AbilityEventType.COMBAT);
    this.bonusChance = bonus;
  }
}
