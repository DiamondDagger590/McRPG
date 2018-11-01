package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.abilities.mining.SuperBreaker;
import us.eunoians.mcmmox.players.McMMOPlayer;

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

  public SuperBreakerEvent(McMMOPlayer player, SuperBreaker superBreaker, int cooldown, double boost, int hasteDuration){
    super(superBreaker, player);
    this.cooldown = cooldown;
    this.boost = boost;
    this.hasteDuration = hasteDuration;
  }
}
