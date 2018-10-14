package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import us.eunoians.mcmmox.abilities.swords.RageSpike;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class RageSpikeDamageEvent extends AbilityActivateEvent{

  @Getter
  private LivingEntity target;
  @Getter @Setter
  private int damage;
  public RageSpikeDamageEvent(McMMOPlayer user, RageSpike rageSpike, LivingEntity target, int damage){
    super(rageSpike, user);
    this.target = target;
    this.damage = damage;
    this.isCancelled = rageSpike.isToggled();
  }
}
