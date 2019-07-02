package us.eunoians.mcrpg.api.util.fishing;

import lombok.Getter;
import org.bukkit.Material;
import us.eunoians.mcrpg.util.Parser;

import java.util.List;

import static us.eunoians.mcrpg.api.util.fishing.FishingItemManager.getFishingLootConfig;

public class FishingItemDep {

  @Getter
  private Material newType;
  @Getter
  private Parser activationEquation;
  @Getter
  private int lowEndAmount;
  @Getter
  private int highEndAmount;
  @Getter
  private String displayName;
  @Getter
  private List<String> lore;
  @Getter
  private int lowTier;
  @Getter
  private int highTier;
  @Getter
  private EnchantmentMeta enchantmentMeta;
  @Getter
  private PotionMeta potionMeta;
  @Getter
  private boolean overrideLowerDependencies;
  @Getter
  private int priority;

  public FishingItemDep(String filePath){
    this.newType = Material.getMaterial(getFishingLootConfig().getString(filePath + "Material", "AIR"));
    this.activationEquation = new Parser(getFishingLootConfig().getString(filePath + "ActivationChance", "50.0"));
    String[] amountRange = getFishingLootConfig().getString(filePath + "Amount", "1").split("-");
    this.lowEndAmount = Integer.parseInt(amountRange[0]);
    this.highEndAmount = amountRange.length > 1 ? Integer.parseInt(amountRange[1]) : lowEndAmount;
    this.displayName = getFishingLootConfig().getString(filePath + "DisplayName", null);
    this.lore = getFishingLootConfig().getStringList(filePath + "Lore");
    String[] tierRange = getFishingLootConfig().getString(filePath + "Tiers", "1").split("-");
    this.lowTier = Integer.parseInt(tierRange[0]);
    this.highTier = tierRange.length > 1 ? Integer.parseInt(tierRange[1]) : lowTier;
    if(getFishingLootConfig().contains(filePath + "EnchantmentMeta")){
      this.enchantmentMeta = new EnchantmentMeta(filePath + "EnchantmentMeta.");
    }
    if(getFishingLootConfig().contains(filePath + "PotionMeta")){
      this.potionMeta = new PotionMeta(filePath + "PotionMeta.");
    }
    this.overrideLowerDependencies = getFishingLootConfig().getBoolean(filePath + "OverrideLowerDependencies", true);
    this.priority = getFishingLootConfig().getInt(filePath + "Priority", 10);
  }
}
