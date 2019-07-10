package us.eunoians.mcrpg.api.events.mcrpg.fishing;

import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import us.eunoians.mcrpg.abilities.fishing.Shake;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ShakeEvent extends AbilityActivateEvent {

  @Getter private LivingEntity target;

  public ShakeEvent(McRPGPlayer mcRPGPlayer, Shake shake, LivingEntity target){
    super(shake, mcRPGPlayer, AbilityEventType.RECREATIONAL);
    this.target = target;
  }
}
