package us.eunoians.mcrpg.api.util.fishing;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.util.Parser;

import java.util.*;

public class FishingItemManager {

  private Map<String, List<FishingItem>> categoriesToItems = new HashMap<>();

  public FishingItemManager(){
    for(String category : getFishingLootConfig().getConfigurationSection("Categories").getKeys(false)){
      List<FishingItem> items = new ArrayList<>();
      for(String item : getFishingLootConfig().getConfigurationSection("Categories." + category).getKeys(false)){
        String path = "Categories." + category + "." + item + ".";
        items.add(new FishingItem(path));
      }
      categoriesToItems.put(category, items);
    }
  }

  public ItemStack generateItem(String category, BaseAbility ability){
    List<FishingItem> items = categoriesToItems.get(category);
    Random rand = new Random();
    for(FishingItem fishingItem : items){
      Parser equation = fishingItem.getChance();
      int tier = ability != null ? ability.getCurrentTier() : 1;
      equation.setVariable("tier", tier);
    }
    return null;
  }

  static FileConfiguration getFishingLootConfig(){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.FISHING_LOOT);
  }
}
