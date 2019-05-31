package us.eunoians.mcrpg.api.events.mcrpg.fitness;

import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import us.eunoians.mcrpg.abilities.fitness.Dodge;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class DodgeEvent extends AbilityActivateEvent {

  @Getter
  private LivingEntity attacker;

  @Getter
  private double damage;

  public DodgeEvent(McRPGPlayer mcRPGPlayer, Dodge dodge, LivingEntity attacker, double damage) {
    super(dodge, mcRPGPlayer, AbilityEventType.COMBAT);
    this.attacker = attacker;
    this.damage = damage;
  }
}
