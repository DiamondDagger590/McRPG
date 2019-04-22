package us.eunoians.mcrpg.api.events.mcrpg.woodcutting;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.abilities.woodcutting.HesperidesApples;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class HesperidesApplesEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private PotionEffectType potionEffectType;

  @Getter
  @Setter
  private int multiplier;

  @Getter
  @Setter
  private int duration;

  @Getter
  @Setter
  private int cooldown;

  @Getter
  private Material itemEaten;

  public HesperidesApplesEvent(McRPGPlayer player, HesperidesApples hesperidesApples, PotionEffectType potionEffectType, int multiplier, int duration, int cooldown, Material itemEaten){
    super(hesperidesApples, player, AbilityEventType.RECREATIONAL);
    this.potionEffectType = potionEffectType;
    this.multiplier = multiplier;
    this.duration = duration;
    this.cooldown = cooldown;
    this.itemEaten = itemEaten;
  }
}
