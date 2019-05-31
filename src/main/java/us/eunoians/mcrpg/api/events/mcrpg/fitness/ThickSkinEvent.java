package us.eunoians.mcrpg.api.events.mcrpg.fitness;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import us.eunoians.mcrpg.abilities.fitness.ThickSkin;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ThickSkinEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double resistanceAmount;

  @Getter
  private LivingEntity enemy;

  public ThickSkinEvent(McRPGPlayer mcRPGPlayer, ThickSkin thickSkin, double resistanceAmount, LivingEntity enemy) {
    super(thickSkin, mcRPGPlayer, AbilityEventType.COMBAT);
    this.resistanceAmount = resistanceAmount;
    this.enemy = enemy;
  }
}
