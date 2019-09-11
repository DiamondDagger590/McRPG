package us.eunoians.mcrpg.api.util.brewing.standmeta;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.brewing.BasePotion;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BrewingGUI {

  @Getter @Setter
  private ItemStack fuel;

  @Getter
  private BrewingStand holder;

  @Getter @Setter
  private ItemStack ingredient;

  @Getter @Setter
  private int currentFuelLevel;

  @Getter @Setter
  private int currentBrewProgress;

  private ItemStack[] specialRewards = new ItemStack[3];

  private BasePotion[] potionItems = new BasePotion[3];

  public List<ItemStack> getSpecialRewards(){
    return Arrays.asList(specialRewards);
  }

  public List<BasePotion> getPotionItems(){
    return Arrays.asList(potionItems);
  }

  public void save(){
    File folder = new File(McRPG.getInstance().getDataFolder(), File.separator + "brewing_storage");
    if(!folder.exists()){
      folder.mkdirs();
    }
    File storageFile = new File(folder, File.separator + convertLocToFileName(holder.getLocation()));
    if(!storageFile.exists()){
      try{
        storageFile.createNewFile();
      }
      catch(IOException e){
        e.printStackTrace();
      }
    }
    FileConfiguration storage = YamlConfiguration.loadConfiguration(storageFile);
    storage.set("Progress", currentBrewProgress);
    storage.set("FuelLv", currentFuelLevel);
    storage.set("Fuel", fuel);
    storage.set("Ing", ingredient);
    for(int i = 0; i < specialRewards.length; i++){
      if(specialRewards[i] != null){
        storage.set("Spec" + (i+1), specialRewards[i]);
      }
    }
    for(int i = 0; i < potionItems.length; i++){
      if(specialRewards[i] != null){
        storage.set("BP" + (i+1), potionItems[i].getAsItem());
      }
    }
  }

  private String convertLocToFileName(Location loc){
    return loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + loc.getWorld().getName();
  }
}
