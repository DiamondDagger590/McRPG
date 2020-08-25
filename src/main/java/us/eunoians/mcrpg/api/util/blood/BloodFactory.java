package us.eunoians.mcrpg.api.util.blood;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BloodFactory{
  
  private static final Random RANDOM = new Random();
  
  public static ItemStack generateBlood(){
    BloodManager bloodManager = BloodManager.getInstance();
    ItemStack blood = new ItemStack(Material.AIR);
    
    List<BloodManager.BloodType> validTypes = new ArrayList<>();
    while(validTypes.isEmpty()){
      for(BloodManager.BloodType bloodType : BloodManager.BloodType.values()){
        double chance = BloodManager.getInstance().getIndividualSpawnChance(bloodType);
        int val = (int) (chance * 1000);
        if(val >= RANDOM.nextInt(100000)){
          validTypes.add(bloodType);
        }
      }
    }
  
    BloodManager.BloodType bloodType = validTypes.get(RANDOM.nextInt(validTypes.size()));
    if(bloodType != null){
      
      BloodWrapper bloodWrapper = BloodManager.getInstance().getBloodWrapper(bloodType);
      FileConfiguration bloodFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BLOOD_FILE);
      String key = "CrystallizedBlood.Types." + bloodType.getId() + ".";
      
      blood = new ItemStack(Material.REDSTONE);
      
      ItemMeta itemMeta = blood.getItemMeta();
      if(bloodFile.contains(key + "DisplayName")){
        itemMeta.setDisplayName(Methods.color(bloodFile.getString(key + "DisplayName")));
      }
      
      List<String> newLore = new ArrayList<>();
      
      if(bloodFile.getBoolean(key + "IsGlowing", true)){
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      }
      blood.setItemMeta(itemMeta);
      
      if(bloodType == BloodManager.BloodType.TOOL){
        int lowEnd = bloodWrapper.getExpMultiplierLowEnd();
        int highEnd = bloodWrapper.getExpMultiplierHighEnd();
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : bloodFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%ExpBoost%", Integer.toString(amount)).replace("%ShatterChance", Double.toString(bloodWrapper.getItemShatterChance()))));
        }
        itemMeta.setLore(newLore);
        blood.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(blood);
        nbtItem.setInteger("ExpBoost", amount);
        nbtItem.setBoolean("McRPGBlood", true);
        nbtItem.setString("BloodType", bloodType.getId());
        blood = nbtItem.getItem();
      }
      else if(bloodType == BloodManager.BloodType.WEAPON){
        int lowEnd = bloodWrapper.getExpMultiplierLowEnd();
        int highEnd = bloodWrapper.getExpMultiplierHighEnd();
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : bloodFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%ExpBoost%", Integer.toString(amount)).replace("%ShatterChance", Double.toString(bloodWrapper.getItemShatterChance()))));
        }
        itemMeta.setLore(newLore);
        blood.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(blood);
        nbtItem.setInteger("ExpBoost", amount);
        nbtItem.setBoolean("McRPGBlood", true);
        nbtItem.setString("BloodType", bloodType.getId());
        blood = nbtItem.getItem();
      }
      else if(bloodType == BloodManager.BloodType.ARMOR){
        int lowEnd = bloodWrapper.getExpMultiplierLowEnd();
        int highEnd = bloodWrapper.getExpMultiplierHighEnd();
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : bloodFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%ExpBoost%", Integer.toString(amount)).replace("%ShatterChance", Double.toString(bloodWrapper.getItemShatterChance()))));
        }
        itemMeta.setLore(newLore);
        blood.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(blood);
        nbtItem.setInteger("ExpBoost", amount);
        nbtItem.setBoolean("McRPGBlood", true);
        nbtItem.setString("BloodType", bloodType.getId());
        blood = nbtItem.getItem();
      }
      if(bloodType == BloodManager.BloodType.CURSE){
        int duration = bloodWrapper.getCurseDuration();
        for(String s : bloodFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%Duration%", Integer.toString(duration))));
        }
        itemMeta.setLore(newLore);
        blood.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(blood);
        nbtItem.setInteger("Duration", duration);
        nbtItem.setBoolean("McRPGBlood", true);
        nbtItem.setString("BloodType", bloodType.getId());
        blood = nbtItem.getItem();
      }
    }
    return blood;
  }
}
