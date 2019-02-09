package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import us.eunoians.mcrpg.abilities.swords.RageSpike;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class RageSpikeDamageEvent extends AbilityActivateEvent{

  @Getter
  private LivingEntity target;
  @Getter @Setter
  private int damage;
  public RageSpikeDamageEvent(McRPGPlayer user, RageSpike rageSpike, LivingEntity target, int damage){
    super(rageSpike, user, AbilityEventType.COMBAT);
    this.target = target;
    this.damage = damage;
    this.isCancelled = !rageSpike.isToggled();
  }
}
