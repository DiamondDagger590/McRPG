package us.eunoians.mcrpg.api.util.brewing.standmeta;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;

public class BrewingStandWrapper {

  private FileConfiguration fileConfiguration;
  @Getter
  private BrewingGUI brewingGUI;
  @Getter
  private BrewingStand brewingStand;

  public BrewingStandWrapper(BrewingStand brewingStand, FileConfiguration fileConfiguration){
    this.brewingGUI = new BrewingGUI(brewingStand, fileConfiguration);
    this.fileConfiguration = fileConfiguration;
    this.brewingStand = brewingStand;
  }

  public void saveToFile(){
    String key = "BrewingStands." + Methods.locToString(brewingStand.getLocation()) + ".";
    fileConfiguration.set(key + "CurrentFuelLevel", brewingGUI.getCurrentFuelLevel());
    fileConfiguration.set(key + "MaxFuelLevel", brewingGUI.getMaxCurrentFuelLevel());
    if(brewingGUI.getFuel() == null || brewingGUI.getFuel().getType() == Material.AIR || brewingGUI.getFuel().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE){
      fileConfiguration.set(key + "FuelItem", null);
    }
    else{
      fileConfiguration.set(key + "FuelItem", brewingGUI.getFuel());
    }
    for(int i = 0; i < brewingGUI.getSpecialRewards().size(); i++){
      ItemStack specialItem = brewingGUI.getSpecialRewards().get(i);
      fileConfiguration.set(key + "BonusItems." + i, specialItem);
    }
    McRPG.getInstance().getBrewingStandManager().save(brewingStand.getChunk());
  }

  public void finishBrew(){
    if(brewingGUI.getBrewTask() != null){
      brewingGUI.cancelBrewTask();
      brewingGUI.finishBrewTask();
    }
  }
  
  public void deleteData(){
    brewingGUI.breakGUI();
    String key = "BrewingStands." + Methods.locToString(brewingStand.getLocation());
    fileConfiguration.set(key, null);
    McRPG.getInstance().getBrewingStandManager().save(brewingStand.getChunk());
  }
}
