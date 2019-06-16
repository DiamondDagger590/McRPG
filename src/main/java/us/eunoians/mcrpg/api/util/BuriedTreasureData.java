package us.eunoians.mcrpg.api.util;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;

import java.util.ArrayList;
import java.util.HashMap;

public class BuriedTreasureData {

  @Getter
  private static HashMap<Material, HashMap<String, ArrayList<BuriedTreasureData.BuriedTreasureItem>>> buriedTreasureData = new HashMap<>();

  public static void init(){
    buriedTreasureData.clear();
    //Yay, now i have this code twice >.>
    FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EXCAVATION_CONFIG);
    for(String cat : config.getConfigurationSection("BuriedTreasureConfig.Categories").getKeys(false)){
      for(String item : config.getConfigurationSection("BuriedTreasureConfig.Categories." + cat).getKeys(false)){
        String key = "BuriedTreasureConfig.Categories." + cat + "." + item;
        int exp = config.getInt(key + ".Exp");
        double dropChance = config.getDouble(key + ".DropChance");
        int maxAmount = config.getInt(key + ".MaxAmount");
        int minAmount = config.getInt(key + ".MinAmount");
        Material itemMaterial = Material.getMaterial(config.getString(key + ".Material"));
        BuriedTreasureData.BuriedTreasureItem buriedTreasureItem = new BuriedTreasureData.BuriedTreasureItem(exp, dropChance, maxAmount, minAmount, itemMaterial);
        for(String block : config.getStringList(key + ".Blocks")){
          Material mat = Material.getMaterial(block);
          if(mat == null) continue;
          if(buriedTreasureData.containsKey(mat)){
            HashMap<String, ArrayList<BuriedTreasureData.BuriedTreasureItem>> categoriesToItems = buriedTreasureData.get(mat);
            if(categoriesToItems.containsKey(cat)){
              buriedTreasureData.get(mat).get(cat).add(buriedTreasureItem);
            }
            else{
              ArrayList<BuriedTreasureData.BuriedTreasureItem> itemsPerCat = new ArrayList<>();
              itemsPerCat.add(buriedTreasureItem);
              categoriesToItems.put(cat, itemsPerCat);
              buriedTreasureData.put(mat, categoriesToItems);
            }
          }
          else{
            HashMap<String, ArrayList<BuriedTreasureData.BuriedTreasureItem>> categoriesToItems = new HashMap<>();
            ArrayList<BuriedTreasureData.BuriedTreasureItem> itemsPerCat = new ArrayList<>();
            itemsPerCat.add(buriedTreasureItem);
            categoriesToItems.put(cat, itemsPerCat);
            buriedTreasureData.put(mat, categoriesToItems);
          }
        }
      }
    }
  }
  public static class BuriedTreasureItem {

    @Getter
    private int exp;

    @Getter
    private double dropChance;

    @Getter
    private int maxAmount;

    @Getter
    private int minAmount;

    @Getter
    private Material material;

    public BuriedTreasureItem(int exp, double dropChance, int maxAmount, int minAmount, Material material){
      this.exp = exp;
      this.dropChance = dropChance;
      this.maxAmount = maxAmount;
      this.minAmount = minAmount;
      this.material = material;
    }
  }
}
