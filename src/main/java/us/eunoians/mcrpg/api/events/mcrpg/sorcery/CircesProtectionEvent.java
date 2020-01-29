package us.eunoians.mcrpg.api.events.mcrpg.sorcery;

import lombok.Getter;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.abilities.sorcery.CircesProtection;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class CircesProtectionEvent extends AbilityActivateEvent{
  
  @Getter
  private PotionEffectType effectToResist;
  
  public CircesProtectionEvent(McRPGPlayer player, CircesProtection circesProtection, PotionEffectType effectToResist){
    super(circesProtection, player, AbilityEventType.RECREATIONAL);
    this.effectToResist = effectToResist;
  }
}
