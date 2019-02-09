package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.mining.SuperBreaker;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class SuperBreakerEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int cooldown;

  @Getter
  @Setter
  private double boost;

  @Getter
  @Setter
  private int hasteDuration;

  public SuperBreakerEvent(McRPGPlayer player, SuperBreaker superBreaker, int cooldown, double boost, int hasteDuration){
    super(superBreaker, player, AbilityEventType.RECREATIONAL);
    this.cooldown = cooldown;
    this.boost = boost;
    this.hasteDuration = hasteDuration;
  }
}
