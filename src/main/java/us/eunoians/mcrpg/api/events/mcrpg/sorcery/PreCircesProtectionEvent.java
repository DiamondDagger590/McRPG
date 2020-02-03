package us.eunoians.mcrpg.api.events.mcrpg.sorcery;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.abilities.sorcery.CircesProtection;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class PreCircesProtectionEvent extends AbilityActivateEvent{
  
  @Getter
  @Setter
  private double resistanceChance;
  
  @Getter
  private PotionEffectType effectToResist;
  
  public PreCircesProtectionEvent(McRPGPlayer player, CircesProtection circesProtection, double resistanceChance, PotionEffectType effectToResist){
    super(circesProtection, player, AbilityEventType.RECREATIONAL);
    this.resistanceChance = resistanceChance;
    this.effectToResist = effectToResist;
  }
}
