package us.eunoians.mcrpg.api.events.mcrpg.taming;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.entity.EntityDamageEvent;
import us.eunoians.mcrpg.abilities.taming.DivineFur;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class DivineFurEvent extends AbilityActivateEvent{
  
  @Getter
  private EntityDamageEvent.DamageCause damageCause;
  
  @Getter @Setter
  private double percentProtected;
  
  public DivineFurEvent(McRPGPlayer mcRPGPlayer, DivineFur divineFur, EntityDamageEvent.DamageCause damageCause, double percentProtected){
    super(divineFur, mcRPGPlayer, AbilityEventType.RECREATIONAL);
    this.damageCause = damageCause;
    this.percentProtected = percentProtected;
  }
}
