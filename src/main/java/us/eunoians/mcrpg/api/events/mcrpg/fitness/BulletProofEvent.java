package us.eunoians.mcrpg.api.events.mcrpg.fitness;

import lombok.Getter;
import org.bukkit.entity.Projectile;
import us.eunoians.mcrpg.abilities.fitness.BulletProof;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class BulletProofEvent extends AbilityActivateEvent {

  @Getter
  private Projectile projectile;

  public BulletProofEvent(McRPGPlayer mcRPGPlayer, BulletProof bulletProof, Projectile projectile) {
    super(bulletProof, mcRPGPlayer, AbilityEventType.COMBAT);
    this.projectile = projectile;
  }
}
