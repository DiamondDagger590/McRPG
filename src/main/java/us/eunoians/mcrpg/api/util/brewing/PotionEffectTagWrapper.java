package us.eunoians.mcrpg.api.util.brewing;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.BasePotionType;

import java.util.HashMap;
import java.util.logging.Level;

public class PotionEffectTagWrapper {

  private HashMap<String, TagMeta> storedTagMeta = new HashMap<>();

  @Getter @Setter
  private boolean canBeSplash;
  @Getter @Setter
  private double splashDurationModifier;
  @Getter @Setter
  private boolean canBeLingering;
  @Getter @Setter
  private double lingeringDurationModifier;

  PotionEffectTagWrapper(BasePotionType effectType){
    FileConfiguration brewingItemConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_ITEMS_CONFIG);
    if(!brewingItemConfig.contains("Potions." + effectType.getName())){
      Bukkit.getLogger().log(Level.WARNING, effectType.getName() + " is invalidly registered in the brewing configuration. It has been ignored. To make this warning go away, please remove or correct the issue.");
      return;
    }
    String refKey = "Potions." + effectType.getName();
    for(String tag : brewingItemConfig.getConfigurationSection("Potions." + effectType.getName()).getKeys(false)){
      TagMeta tagMeta = new TagMeta(refKey, tag);
      storedTagMeta.put(tag, tagMeta);
    }

    String key = refKey + ".SpecialModifiers.";
    canBeSplash = brewingItemConfig.getBoolean(key + "CanBeSplash", false);
    canBeLingering = brewingItemConfig.getBoolean(key + "CanBeLingering", false);
    splashDurationModifier = brewingItemConfig.getDouble(key + "SplashDurationModifier", 0.5d);
    lingeringDurationModifier = brewingItemConfig.getDouble(key + "LingeringDurationModifier", 0.3d);
  }
}
