package us.eunoians.mcrpg.api.events.mcrpg.axes;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.axes.WhirlwindStrike;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class WhirlwindStrikeEvent extends AbilityActivateEvent {

  @Getter @Setter private int damage;
  @Getter @Setter private int range;
  @Getter @Setter private int cooldown;

  public WhirlwindStrikeEvent(McRPGPlayer player, WhirlwindStrike whirlwindStrike, int damage, int range, int cooldown){
    super(whirlwindStrike, player, AbilityEventType.COMBAT);
    this.damage = damage;
    this.range = range;
    this.cooldown = cooldown;
  }
}
