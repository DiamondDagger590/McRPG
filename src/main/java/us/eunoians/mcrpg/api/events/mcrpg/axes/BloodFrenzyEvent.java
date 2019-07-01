package us.eunoians.mcrpg.api.events.mcrpg.axes;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import us.eunoians.mcrpg.abilities.axes.BloodFrenzy;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class BloodFrenzyEvent extends AbilityActivateEvent {

  @Getter private LivingEntity target;
  @Getter @Setter private int hasteDuration;
  @Getter @Setter private int hasteLevel;
  @Getter @Setter private int regenDuration;
  @Getter @Setter private int regenLevel;

  public BloodFrenzyEvent(McRPGPlayer player, BloodFrenzy bloodFrenzy, int hasteDuration, int hasteLevel, int regenDuration, int regenLevel, LivingEntity target){
    super(bloodFrenzy, player, AbilityEventType.COMBAT);
    this.hasteDuration = hasteDuration;
    this.hasteLevel = hasteLevel;
    this.regenDuration = regenDuration;
    this.regenLevel = regenLevel;
    this.target = target;
  }
}
