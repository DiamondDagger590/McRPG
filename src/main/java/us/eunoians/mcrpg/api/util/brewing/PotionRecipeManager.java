package us.eunoians.mcrpg.api.util.brewing;


import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.BasePotionType;

import java.util.HashMap;
import java.util.Map;

public class PotionRecipeManager {

  private Map<Material, Integer> fuelMaterials = new HashMap<>();
  private Map<BasePotionType, PotionEffectTagWrapper> potionRecipeMap = new HashMap<>();

  public PotionRecipeManager(){
    reloadManager();
  }

  public void reloadManager(){
    initRecipes();
    initFuelItems();
  }

  private void initRecipes(){
    potionRecipeMap.clear();
    FileConfiguration brewingItemConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_ITEMS_CONFIG);
    for(String s : brewingItemConfig.getConfigurationSection("Potions").getKeys(false)){
      PotionEffectType potionEffectType = PotionEffectType.getByName(s);
      BasePotionType basePotionType = BasePotionType.getFromPotionEffect(potionEffectType);
      PotionEffectTagWrapper potionEffectTagWrapper = new PotionEffectTagWrapper(basePotionType);
      potionRecipeMap.put(basePotionType, potionEffectTagWrapper);
    }
  }

  private void initFuelItems(){
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

  public boolean isPotionTypeRegistered(BasePotionType potionType){ return potionRecipeMap.containsKey(potionType); }

  public PotionEffectTagWrapper getPotionEffectTagWrapper(BasePotionType potionType){ return potionRecipeMap.get(potionType); }

  public boolean doesMaterialLeadToChild(Material material, ItemStack potion){
    NBTItem nbtItem = new NBTItem(potion);
    org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) potion.getItemMeta();
    PotionEffectType effectType = meta.getBasePotionData().getType().getEffectType();
    BasePotionType basePotionType = BasePotionType.getFromPotionEffect(effectType);

    if(nbtItem.hasKey("McRPGTag")){

    }
    else{

    }
    return false;
  }


  //Validate legacy potions
  public String validateTag(ItemStack potion){
    return "";
  }
}
