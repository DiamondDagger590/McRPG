package us.eunoians.mcrpg.api.util.fishing;

import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import us.eunoians.mcrpg.api.util.Methods;

import java.util.*;

import static us.eunoians.mcrpg.api.util.fishing.FishingItemManager.getFishingLootConfig;

public class EnchantmentMeta {

  @Getter
  private ArrayList<FishingItemEnchant> enchants = new ArrayList<>();
  @Getter
  private boolean overrideExistingEnchants;
  @Getter
  private int lowEndEnchantAmount;
  @Getter
  private int highEndEnchantAmount;


  public EnchantmentMeta(String filePath){
    this.overrideExistingEnchants = getFishingLootConfig().getBoolean(filePath + "OverrideExistingEnchants", false);
    String[] enchantmentRange = getFishingLootConfig().getString(filePath + "EnchantmentAmountRange", "1").split("-");
    this.lowEndEnchantAmount = Integer.parseInt(enchantmentRange[0]);
    this.highEndEnchantAmount = enchantmentRange.length > 1 ? Integer.parseInt(enchantmentRange[1]) : lowEndEnchantAmount;
    if(getFishingLootConfig().contains(filePath + "Enchantments")){
      for(String enchant : getFishingLootConfig().getStringList(filePath + "Enchantments")){
        String[] enchantmentData = enchant.split(":");
        Enchantment enchantmentType = Enchantment.getByName(enchantmentData[0]);
        String[] enchantmentLevelRange = enchantmentData[1].split("-");
        int lowLevel = Integer.parseInt(enchantmentLevelRange[0]);
        int highLevel = enchantmentLevelRange.length > 1 ? Integer.parseInt(enchantmentLevelRange[1]) : lowLevel;
        int chance = Integer.parseInt(enchantmentData[2]);
        enchants.add(new FishingItemEnchant(enchantmentType, lowLevel, highLevel, chance));
      }
    }
  }

  public Map<Enchantment, Integer> generateEnchantmentMap(){
    Map<Enchantment, Integer> returnMap = new HashMap<>();
    Random rand = new Random();
    //Attempt to populate map
    for(FishingItemEnchant enchant : enchants){
      if(enchant.getEnchantmentChance() >= rand.nextInt(100)){
        int weightedSum = 0;
        int level = 0;
        //Map of enchant level to weight
        Map<Integer, Integer> sums = new HashMap<>();
        //Populate map with weights
        for(int i = enchant.getLowLevel(); i <= enchant.getHighLevel(); i++){
          int val = getFishingLootConfig().getInt("EnchantmentWeightScale." + Methods.convertToNumeral(i), 0);
          weightedSum += val;
          sums.put(i, val);
        }
        //our weighted value
        int i = rand.nextInt(weightedSum);
        int temp = 0;
        for(int x = enchant.getLowLevel(); x <= enchant.getHighLevel(); x++){
          //increase the temp counter
          temp += sums.get(x);
          //if i is in that range then we set level to x and break
          if(i <= temp){
            level = x;
            break;
          }
        }
        if(returnMap.size() >= highEndEnchantAmount){
          if(rand.nextInt(1) == 1){
            Enchantment toRemove = (Enchantment) returnMap.keySet().toArray()[rand.nextInt(returnMap.size())];
            returnMap.remove(toRemove);
            returnMap.put(enchant.getEnchantmentType(), level);
          }
          continue;
        }
        returnMap.put(enchant.getEnchantmentType(), level);
      }
    }
    //Failsafe if no enchantments were found
    List<FishingItemEnchant> clone = (List<FishingItemEnchant>) enchants.clone();
    if(returnMap.size() < lowEndEnchantAmount){
      while(returnMap.size() < lowEndEnchantAmount && clone.size() > 0){
        FishingItemEnchant enchant = clone.get(rand.nextInt(clone.size()));
        if(enchant.getEnchantmentChance() >= rand.nextInt(100)){
          int level = enchant.getLowLevel() + rand.nextInt(enchant.getHighLevel() - enchant.getLowLevel());
          returnMap.put(enchant.getEnchantmentType(), level);
          clone.remove(enchant);
        }
      }
    }
    return returnMap;
  }
}
