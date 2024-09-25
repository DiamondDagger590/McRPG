package us.eunoians.mcrpg.api.util.brewing;


import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.BasePotionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PotionRecipeManager {

  private Map<Material, Integer> fuelMaterials = new HashMap<>();
  private Map<BasePotionType, PotionEffectTagWrapper> potionRecipeMap = new HashMap<>();
  private Set<Material> allPossibleIngredients = new HashSet<>();
  private Set<BasePotionType> extraRecipes = new HashSet<>();
  private HashMap<Integer, Set<BasePotionType>> tierToTypes = new HashMap<>();

  public PotionRecipeManager(){
    reloadManager();
  }

  public void reloadManager(){
    initRecipes();
    initFuelItems();
  }

  private void initRecipes(){
    potionRecipeMap.clear();
    extraRecipes.clear();
    tierToTypes.clear();
    allPossibleIngredients.add(Material.NETHER_WART);
    allPossibleIngredients.add(Material.FERMENTED_SPIDER_EYE);
    allPossibleIngredients.add(Material.GUNPOWDER);
    allPossibleIngredients.add(Material.DRAGON_BREATH);
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
    FileConfiguration sorceryConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
    int maxTier = sorceryConfig.getInt("CircesRecipesConfig.TierAmount");
    for(int i = 1; i <= maxTier; i++){
      Set<BasePotionType> typeSet = new HashSet<>();
      for(String effect : sorceryConfig.getStringList("CircesRecipesConfig.Tier" + Methods.convertToNumeral(i) + ".PotionEffects")){
        PotionEffectType effectType = PotionEffectType.getByName(effect);
        BasePotionType basePotionType = BasePotionType.getFromPotionEffect(effectType);
        extraRecipes.add(basePotionType);
        typeSet.add(basePotionType);
      }
      tierToTypes.put(i, typeSet);
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

  public boolean isLockedRecipe(BasePotionType basePotionType){
    return extraRecipes.contains(basePotionType);
  }
  
  public Set<BasePotionType> getTypesForTier(int tier){
    return tierToTypes.get(tier);
  }
  
  public int getFuelAmount(ItemStack fuel){
    return fuelMaterials.get(fuel.getType());
  }

  public boolean isPotionTypeRegistered(BasePotionType potionType){ return potionRecipeMap.containsKey(potionType); }

  public PotionEffectTagWrapper getPotionEffectTagWrapper(BasePotionType potionType){ return potionRecipeMap.get(potionType); }

  public boolean doesMaterialLeadToChild(Material ingredient, BasePotion basePotion){
    if(basePotion.getBasePotionType() == BasePotionType.WATER && (ingredient == Material.NETHER_WART || ingredient == Material.FERMENTED_SPIDER_EYE)){
      return true;
    }
    PotionEffectTagWrapper potionEffectTagWrapper = getPotionEffectTagWrapper(basePotion.getBasePotionType());
    if(basePotion.getAsItem().getType() == Material.POTION && ingredient == Material.GUNPOWDER && potionEffectTagWrapper.isCanBeSplash()){
      return true;
    }
    else if(basePotion.getAsItem().getType() == Material.SPLASH_POTION && ingredient == Material.DRAGON_BREATH && potionEffectTagWrapper.isCanBeLingering()){
      return true;
    }
    String tag = basePotion.getTag();
    TagMeta tagMeta = potionEffectTagWrapper.getTagMeta(tag);
    return tag != null && tagMeta != null && tagMeta.getChildTag(ingredient) != null;
  }
  
  public BasePotionType getChildPotionType(Material ingredient, BasePotion basePotion){
    if(basePotion.getBasePotionType() == BasePotionType.WATER && ingredient == Material.NETHER_WART){
      return BasePotionType.AWKWARD;
    }
    else if(basePotion.getBasePotionType() == BasePotionType.WATER && ingredient == Material.FERMENTED_SPIDER_EYE){
      return BasePotionType.WEAKNESS;
    }
    PotionEffectTagWrapper potionEffectTagWrapper = getPotionEffectTagWrapper(basePotion.getBasePotionType());
    if(basePotion.getAsItem().getType() == Material.POTION && ingredient == Material.GUNPOWDER && potionEffectTagWrapper.isCanBeSplash()){
      return basePotion.getBasePotionType();
    }
    else if(basePotion.getAsItem().getType() == Material.SPLASH_POTION && ingredient == Material.DRAGON_BREATH && potionEffectTagWrapper.isCanBeLingering()){
      return basePotion.getBasePotionType();
    }
    String tag = basePotion.getTag();
    TagMeta tagMeta = potionEffectTagWrapper.getTagMeta(tag);
    String child = tagMeta.getChildTag(ingredient);
    if(child.split("\\.").length >= 2){
      return BasePotionType.getFromPotionEffect(PotionEffectType.getByName(child.split("\\.")[0]));
    }
    else{
      return tagMeta.getBasePotionType();
    }
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
