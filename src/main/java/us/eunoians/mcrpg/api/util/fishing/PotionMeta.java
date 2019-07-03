package us.eunoians.mcrpg.api.util.fishing;

import lombok.Getter;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

import static us.eunoians.mcrpg.api.util.fishing.FishingItemManager.getFishingLootConfig;

public class PotionMeta {

  @Getter
  private List<PotionSubMeta> effects = new ArrayList<>();
  @Getter
  private boolean isCustomPotion;
  @Getter
  private PotionType potionType;
  @Getter
  private boolean extended;
  @Getter
  private boolean upgraded;
  @Getter
  private boolean isSplash;
  @Getter
  private boolean isLingering;
  @Getter
  private String RGB;

  public PotionMeta(String filePath){
    if(getFishingLootConfig().contains(filePath + "Effects")){
      isCustomPotion = true;
      for(String s : getFishingLootConfig().getStringList(filePath + "Effects")){
        effects.add(new PotionSubMeta(s));
      }
    }
    else{
      this.potionType = PotionType.valueOf(getFishingLootConfig().getString(filePath + "PotionType", "WATER"));
      this.extended = getFishingLootConfig().getBoolean(filePath + "Extended", false);
      this.upgraded = getFishingLootConfig().getBoolean(filePath + "Upgraded", false);
    }
    isSplash = getFishingLootConfig().getBoolean(filePath + "Splash", false);
    isLingering = getFishingLootConfig().getBoolean(filePath + "Lingering", false);
    RGB = getFishingLootConfig().getString(filePath + "RGB", "");
  }

  class PotionSubMeta {

    @Getter
    private PotionEffectType effectType;
    @Getter
    private int duration;
    @Getter
    private int level;

    PotionSubMeta(String effect){
      String[] potionData = effect.split(":");
      this.effectType = PotionEffectType.getByName(potionData[0]);
      this.duration = Integer.parseInt(potionData[2]);
      this.level = Integer.parseInt(potionData[1]);
    }
  }
}
