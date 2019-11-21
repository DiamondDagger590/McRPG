package us.eunoians.mcrpg.api.util.brewing;


import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;

import java.util.HashMap;
import java.util.Map;

public class PotionRecipeManager {

  private Map<Material, Integer> fuelMaterials = new HashMap<>();


  public void initFuelItems(){
    FileConfiguration brewingItemsConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_ITEMS_CONFIG);
    fuelMaterials.clear();
    for(String type : brewingItemsConfig.getConfigurationSection("Fuel").getKeys(false)){
      String key = "Fuel." + type + ".";
      Material mat = Material.getMaterial(brewingItemsConfig.getString(key + "Material"));
      int fuelAmount = brewingItemsConfig.getInt(key + "FuelAmount");
      fuelMaterials.put(mat, fuelAmount);
    }
  }

  public boolean isFuel(ItemStack fuel){
    return fuelMaterials.containsKey(fuel.getType());
  }

  public int getFuelAmount(ItemStack fuel){
    return fuelMaterials.get(fuel.getType());
  }
}
