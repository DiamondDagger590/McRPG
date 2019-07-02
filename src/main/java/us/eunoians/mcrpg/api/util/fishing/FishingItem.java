package us.eunoians.mcrpg.api.util.fishing;

import lombok.Getter;
import org.bukkit.Material;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static us.eunoians.mcrpg.api.util.fishing.FishingItemManager.getFishingLootConfig;

public class FishingItem {

  @Getter private Material itemType;
  @Getter private Parser chance;
  @Getter private int lowEndAmount;
  @Getter private int highEndAmount;
  @Getter private int lowEndDurability;
  @Getter private int highEndDurability;
  @Getter private int lowEndVanillaExpAmount;
  @Getter private int highEndVanillaExpAmount;
  @Getter private int mcrpgExpValue;
  @Getter private String displayName;
  @Getter private List<String> lore;
  @Getter private EnchantmentMeta enchantmentMeta;
  @Getter private PotionMeta potionMeta;
  @Getter private Map<UnlockedAbilities, FishingItemDep> dependancies = new HashMap<>();

  public FishingItem(String filePath){
    this.itemType = Material.getMaterial(getFishingLootConfig().getString(filePath + "Material", "AIR"));
    this.chance = new Parser(getFishingLootConfig().getString(filePath + "Chance", "1.0"));
    String[] amountRange = getFishingLootConfig().getString(filePath + "Amount", "1").split("-");
    this.lowEndAmount = Integer.parseInt(amountRange[0]);
    this.highEndAmount = amountRange.length > 1 ? Integer.parseInt(amountRange[1]) : lowEndAmount;
    String[] durabilityRange = getFishingLootConfig().getString(filePath + "DurabilityRange", "0").split("-");
    this.lowEndDurability = Integer.parseInt(durabilityRange[0]);
    this.highEndDurability = durabilityRange.length > 1 ? Integer.parseInt(durabilityRange[1]) : lowEndDurability;
    String[] expRange = getFishingLootConfig().getString(filePath + "VanillaExp", "0").split("-");
    this.lowEndVanillaExpAmount = Integer.parseInt(expRange[0]);
    this.highEndVanillaExpAmount = expRange.length > 1 ? Integer.parseInt(expRange[1]) : highEndAmount;
    this.mcrpgExpValue = getFishingLootConfig().getInt(filePath + "McRPGExp", 0);
    this.displayName = getFishingLootConfig().getString(filePath + "DisplayName", "");
    if(getFishingLootConfig().contains(filePath + "Lore")){
      this.lore = getFishingLootConfig().getStringList(filePath + "Lore");
    }
    if(getFishingLootConfig().contains(filePath + "EnchantmentMeta")){
      this.enchantmentMeta = new EnchantmentMeta(filePath + "EnchantmentMeta.");
    }
    if(getFishingLootConfig().contains(filePath + "PotionMeta")){
      this.potionMeta = new PotionMeta(filePath + "PotionMeta.");
    }
    if(getFishingLootConfig().contains(filePath + "Dependencies")){
      for(String dep : getFishingLootConfig().getConfigurationSection(filePath + "Dependencies").getKeys(false)){
        if(UnlockedAbilities.isAbility(dep)){
          UnlockedAbilities unlockedAbility = UnlockedAbilities.fromString(dep);
          FishingItemDep fishingItemDep = new FishingItemDep(filePath + "Dependencies." + dep + ".");
          dependancies.put(unlockedAbility, fishingItemDep);
        }
        else{
          //TODO log error
        }
      }
    }
  }
}
