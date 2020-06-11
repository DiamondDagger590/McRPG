package us.eunoians.mcrpg.api.events.mcrpg.taming;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.taming.FuryOfCerebus;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class FuryOfCerebusEvent extends AbilityActivateEvent{
  
  @Getter @Setter
  private int hellHoundHealth;
  
  @Getter @Setter
  private boolean igniteTarget;
  
  @Getter @Setter
  private boolean explosionDestroyBlocks;
  
  @Getter @Setter
  private int selfDestructTimer;
  
  @Getter @Setter
  private int cooldown;
  
  public FuryOfCerebusEvent(McRPGPlayer mcRPGPlayer, FuryOfCerebus furyOfCerebus, int hellHoundHealth, boolean igniteTarget, boolean explosionDestroyBlocks, int selfDestructTimer, int cooldown){
    super(furyOfCerebus, mcRPGPlayer, AbilityEventType.COMBAT);
    this.hellHoundHealth = hellHoundHealth;
    this.igniteTarget = igniteTarget;
    this.explosionDestroyBlocks = explosionDestroyBlocks;
    this.selfDestructTimer = selfDestructTimer;
    this.cooldown = cooldown;
  }
}
