package us.eunoians.mcrpg.api.util.fishing;

import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

public class FishingItemEnchant {

  @Getter
  private Enchantment enchantmentType;
  @Getter
  private int lowLevel;
  @Getter
  private int highLevel;
  @Getter
  private double enchantmentChance;

  public FishingItemEnchant(Enchantment enchantmentType, int lowLevel, int highLevel, double enchantmentChance){
    this.enchantmentType = enchantmentType;
    this.lowLevel = lowLevel;
    this.highLevel = highLevel;
    this.enchantmentChance = enchantmentChance;
  }
}
