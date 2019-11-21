package us.eunoians.mcrpg.api.util.brewing;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;

import java.util.HashMap;
import java.util.logging.Level;

public class PotionEffectTagWrapper {

  private HashMap<String, TagMeta> storedTagMeta = new HashMap<>();

  @Getter
  @Setter
  private boolean canBeSplash = false;
  @Getter @Setter
  private double splashDurationModifier = 0.5d;
  @Getter @Setter
  private boolean canBeLingering = false;
  @Getter @Setter
  private double lingeringDurationModifier = 0.3d;

  public PotionEffectTagWrapper(PotionEffectType effectType){
    FileConfiguration brewingItemConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_ITEMS_CONFIG);
    if(!brewingItemConfig.contains("Potions." + effectType.getName())){
      Bukkit.getLogger().log(Level.WARNING, effectType.getName() + " is invalidly registered in the brewing configuration. It has been ignored. To make this warning go away, please remove or correct the issue.");
      return;
    }
    String key = "Potions." + effectType.getName() + ".";
    for(String tag : brewingItemConfig.getConfigurationSection("Potions." + effectType.getName()).getKeys(false)){

    }
  }
}
