package us.eunoians.mcrpg.api.util.brewing;


import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.BasePotionType;

import java.util.*;

public class PotionRecipeManager {

  private Map<Material, Integer> fuelMaterials = new HashMap<>();
  private Map<BasePotionType, PotionEffectTagWrapper> potionRecipeMap = new HashMap<>();
  private Set<Material> allPossibleIngredients = new HashSet<>(Collections.singletonList(Material.NETHER_WART));

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
      BasePotionType basePotionType;
      if(s.equals("WATER")){
        basePotionType = BasePotionType.WATER;
      }
      else if(s.equals("AWKWARD")){
        basePotionType = BasePotionType.AWKWARD;
      }
      else{
        PotionEffectType potionEffectType = PotionEffectType.getByName(s);
        basePotionType = BasePotionType.getFromPotionEffect(potionEffectType);
      }
      PotionEffectTagWrapper potionEffectTagWrapper = new PotionEffectTagWrapper(basePotionType);
      potionRecipeMap.put(basePotionType, potionEffectTagWrapper);
      allPossibleIngredients.addAll(potionEffectTagWrapper.getAllChildIgredients());
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

  public boolean doesMaterialLeadToChild(Material ingredient, BasePotion basePotion){
    if(basePotion.getBasePotionType() == BasePotionType.WATER && ingredient == Material.NETHER_WART){
      return true;
    }
    PotionEffectTagWrapper potionEffectTagWrapper = getPotionEffectTagWrapper(basePotion.getBasePotionType());
    String tag = basePotion.getTag();
    TagMeta tagMeta = potionEffectTagWrapper.getTagMeta(tag);
    return tagMeta.getChildTag(ingredient) != null;
  }

  public void updateInformation(Material ingredient, BasePotion basePotion){
    if(basePotion.getBasePotionType() == BasePotionType.WATER && ingredient == Material.NETHER_WART){
      basePotion.setBasePotionType(BasePotionType.AWKWARD);

      return;
    }
  }

  public boolean isValidIngredient(Material ingredient){
    return allPossibleIngredients.contains(ingredient);
  }

  //Validate legacy potions
  public String validateTag(ItemStack potion){
    return "";
  }
}
