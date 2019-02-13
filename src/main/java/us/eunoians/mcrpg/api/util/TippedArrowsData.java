package us.eunoians.mcrpg.api.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.potion.PotionEffectType;

public class TippedArrowsData {

  @Getter
  @Setter
  private PotionEffectType potionEffect;
  @Getter
  @Setter
  private int duration;
  @Getter
  @Setter
  private int potency;

  public TippedArrowsData(PotionEffectType potionEffect, int duration, int potency){
    this.potionEffect = potionEffect;
    this.duration = duration;
    this.potency = potency;
  }
}
