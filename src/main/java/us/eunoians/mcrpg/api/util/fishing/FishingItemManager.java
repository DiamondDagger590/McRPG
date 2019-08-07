package us.eunoians.mcrpg.api.util.fishing;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.Parser;

import java.util.*;

public class FishingItemManager {

  private Map<String, List<FishingItem>> categoriesToItems = new HashMap<>();
  private Map<EntityType, List<ShakeItem>> shakeItems = new HashMap<>();
  private static Random rand = new Random();
  private int lowDurability = 0;
  private int highDurability = 0;

  public FishingItemManager(){
    for(String category : getFishingLootConfig().getConfigurationSection("Categories").getKeys(false)){
      List<FishingItem> items = new ArrayList<>();
      for(String item : getFishingLootConfig().getConfigurationSection("Categories." + category).getKeys(false)){
        String path = "Categories." + category + "." + item + ".";
        items.add(new FishingItem(path));
      }
      categoriesToItems.put(category, items);
    }
    for(String entity : getFishingLootConfig().getConfigurationSection("ShakeLootTable").getKeys(false)){
      EntityType type = EntityType.fromName(entity);
      List<ShakeItem> items = new ArrayList<>();
      for(String item : getFishingLootConfig().getConfigurationSection("ShakeLootTable." + entity).getKeys(false)){
        items.add(new ShakeItem("ShakeLootTable." + entity + "." + item + "."));
      }
      shakeItems.put(type, items);
    }
    String[] durabilityData = getFishingLootConfig().getString("DurabilityDamageScale").split("-");
    lowDurability = Integer.parseInt(durabilityData[0]);
    highDurability = durabilityData.length > 1 ? Integer.parseInt(durabilityData[1]) : 60;
  }

  public FishingResult generateItem(String category, BaseAbility ability, McRPGPlayer player){
    List<FishingItem> items = categoriesToItems.get(category);
    Random rand = new Random();
    //List of possible items that were found
    List<FishingItem> returnItems = new ArrayList<>();
    while(returnItems.isEmpty()) {
      for (FishingItem fishingItem : items) {
        Parser equation = fishingItem.getChance();
        int tier = (ability != null && ability.getCurrentTier() != 0) ? ability.getCurrentTier() : 1;
        equation.setVariable("tier", tier);
        int chance = (int) (equation.getValue() * 1000);
        int val = rand.nextInt(100000);
        if (chance >= val) {
          returnItems.add(fishingItem);
        }
      }
    }
    //Get a random item from results
    FishingItem resultItem = returnItems.get(rand.nextInt(returnItems.size()));
    //Generate the item
    Material mat = resultItem.getItemType();
    boolean isEnchanted = resultItem.getEnchantmentMeta() != null;
    boolean isPotion = resultItem.getPotionMeta() != null;
    String displayName = resultItem.getDisplayName();
    List<String> lore = resultItem.getLore();
    if(mat == null){
      Bukkit.broadcastMessage(category + "" + returnItems.size());
    }
    if(mat == Material.AIR || mat == null){
      return new FishingResult(new ItemStack(Material.COD), 1, 10);
    }
    ItemStack returnItem = FishedItemStackFactory.createItem(mat, displayName, lore);
    if(isEnchanted){
      returnItem = FishedItemStackFactory.enchantItem(returnItem, resultItem.getEnchantmentMeta());
    }
    else if(isPotion){
      returnItem = FishedItemStackFactory.convertToPotion(returnItem, resultItem.getPotionMeta());
    }
    returnItem = FishedItemStackFactory.damageItem(returnItem, lowDurability, highDurability);
   /* if(resultItem.getLowEndDurability() > 0){
      returnItem = FishedItemStackFactory.damageItem(returnItem, resultItem.getLowEndDurability(), resultItem.getHighEndDurability());
    }*/
    if(resultItem.getHighEndAmount() > 1){
      returnItem.setAmount(resultItem.getLowEndAmount() + rand.nextInt(resultItem.getHighEndAmount() - resultItem.getLowEndAmount()));
    }
    int vanillaExp = resultItem.getLowEndVanillaExpAmount();
    if(resultItem.getHighEndVanillaExpAmount() > 0){
      vanillaExp = vanillaExp + rand.nextInt(resultItem.getHighEndVanillaExpAmount() - vanillaExp);
    }

    //Sort through any dependencies
    List<FishingItemDep> validDeps = new ArrayList<>();
    for(UnlockedAbilities ab : resultItem.getDependancies().keySet()){
      if(player.getAbilityLoadout().contains(ab)){
        FishingItemDep fishingItemDep = resultItem.getDependancies().get(ab);
        if(player.getBaseAbility(ab).getCurrentTier() >= fishingItemDep.getLowTier() && player.getBaseAbility(ab).getCurrentTier() <= fishingItemDep.getHighTier()){
          Parser equation = fishingItemDep.getActivationEquation();
          equation.setVariable("tier", player.getBaseAbility(ab).getCurrentTier());
          int chance = (int) (equation.getValue() * 1000);
          int val = rand.nextInt(100000);
          if(chance >= val){
            validDeps.add(fishingItemDep);
          }
        }
      }
    }

    validDeps.sort(Comparator.comparingInt(FishingItemDep::getPriority));

    boolean displayNameEdited = false;
    boolean loreEdited = false;
    boolean potionMetaEdited = false;
    boolean enchantmentMetaEdited = false;
    boolean amountEdited = false;
    boolean materialEdited = false;
    boolean damageEdited = false;
    for(FishingItemDep dep : validDeps){

      ItemMeta resultMeta = returnItem.getItemMeta();
      assert resultMeta != null;

      boolean override = dep.isOverrideLowerDependencies();
      if(dep.getDisplayName() != null && (!displayNameEdited || override)){
        resultMeta.setDisplayName(Methods.color(dep.getDisplayName()));
        displayNameEdited = true;
      }
      if (dep.getLore() != null && (!loreEdited || override)) {
        resultMeta.setLore(Methods.colorLore(dep.getLore()));
        loreEdited = true;
      }
      returnItem.setItemMeta(resultMeta);
      if(dep.getPotionMeta() != null && (!potionMetaEdited || override)){
        returnItem = FishedItemStackFactory.convertToPotion(returnItem, dep.getPotionMeta());
        potionMetaEdited = true;
      }
      if(dep.getEnchantmentMeta() != null && (!enchantmentMetaEdited || override)){
        returnItem = FishedItemStackFactory.enchantItem(returnItem, dep.getEnchantmentMeta());
        enchantmentMetaEdited = true;
      }
      if(dep.getHighEndAmount() > 1 && (!amountEdited || override)){
        returnItem.setAmount(dep.getLowEndAmount() + rand.nextInt(dep.getHighEndAmount() - dep.getLowEndAmount()));
        amountEdited = true;
      }
      if(dep.getNewType() != Material.AIR && (!materialEdited || override)){
        returnItem.setType(dep.getNewType());
        materialEdited = true;
      }
      if(dep.getHighDamage() != 0 && (!damageEdited || override)){
        returnItem = FishedItemStackFactory.damageItem(returnItem, dep.getLowDamage(), dep.getHighDamage());
        damageEdited = true;
      }
    }

    return new FishingResult(returnItem, vanillaExp, resultItem.getMcrpgExpValue());
  }

  public ShakeResult getShakeItem(EntityType entityType){
    List<ShakeItem> validItems = new ArrayList<>();
    while(validItems.isEmpty()) {
      for (ShakeItem shakeItem : shakeItems.get(entityType)) {
        int chance = (int) (shakeItem.getChance() * 1000);
        int val = rand.nextInt(100000);
        if (chance >= val) {
          validItems.add(shakeItem);
        }
      }
    }
    ShakeItem result = validItems.get(rand.nextInt(validItems.size()));
    ItemStack returnItem = FishedItemStackFactory.createItem(result.getType(), result.getDisplayName(), result.getLore());
    if(result.getEnchantmentMeta() != null){
      returnItem = FishedItemStackFactory.enchantItem(returnItem, result.getEnchantmentMeta());
    }
    if(result.getPotionMeta() != null){
      returnItem = FishedItemStackFactory.convertToPotion(returnItem, result.getPotionMeta());
    }
    int amount = result.getLowEndAmount();
    if(result.getHighEndAmount() > 1){
      amount += rand.nextInt(result.getHighEndAmount() - amount);
    }
    returnItem.setAmount(amount);
    return new ShakeResult(returnItem, result.getExp());
  }

  public boolean canShake(EntityType entityType){
    return shakeItems.containsKey(entityType);
  }

  static FileConfiguration getFishingLootConfig(){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.FISHING_LOOT);
  }
}
