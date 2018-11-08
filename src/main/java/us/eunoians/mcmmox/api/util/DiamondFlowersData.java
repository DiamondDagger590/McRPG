package us.eunoians.mcmmox.api.util;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcmmox.Mcmmox;

import java.util.ArrayList;
import java.util.HashMap;

public class DiamondFlowersData {

  @Getter
  private static HashMap<Material, HashMap<String, ArrayList<DiamondFlowersItem>>> diamondFlowersData = new HashMap<>();

  public static void init(){
    diamondFlowersData.clear();
    //Im so sorry to anyone reading this. I hate it just as much as you do
	FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
	for(String cat : config.getConfigurationSection("DiamondFlowersConfig.Categories").getKeys(false)){
	  for(String item : config.getConfigurationSection("DiamondFlowersConfig.Categories." + cat).getKeys(false)){
	    String key = "DiamondFlowersConfig.Categories." + cat + "." + item;
		int exp = config.getInt(key + ".Exp");
		double dropChance = config.getDouble(key + ".DropChance");
		int maxAmount = config.getInt(key + ".MaxAmount");
		int minAmount = config.getInt(key + ".MinAmount");
		Material itemMaterial = Material.getMaterial(config.getString(key + ".Material"));
		DiamondFlowersItem diamondFlowersItem = new DiamondFlowersItem(exp, dropChance, maxAmount, minAmount, itemMaterial);
	    for(String block : config.getStringList(key + ".Blocks")){
	      Material mat = Material.getMaterial(block);
	      if(mat == null) continue;
		  if(diamondFlowersData.containsKey(mat)){
			HashMap<String, ArrayList<DiamondFlowersItem>> categoriesToItems = diamondFlowersData.get(mat);
			if(categoriesToItems.containsKey(cat)){
			  diamondFlowersData.get(mat).get(cat).add(diamondFlowersItem);
			}
			else{
			  ArrayList<DiamondFlowersItem> itemsPerCat = new ArrayList<>();
			  itemsPerCat.add(diamondFlowersItem);
			  categoriesToItems.put(cat, itemsPerCat);
			  diamondFlowersData.put(mat, categoriesToItems);
			}
		  }
		  else{
			HashMap<String, ArrayList<DiamondFlowersItem>> categoriesToItems = new HashMap<>();
			ArrayList<DiamondFlowersItem> itemsPerCat = new ArrayList<>();
			itemsPerCat.add(diamondFlowersItem);
			categoriesToItems.put(cat, itemsPerCat);
			diamondFlowersData.put(mat, categoriesToItems);
		  }
		}
	  }
	}
  }
  public static class DiamondFlowersItem {

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

	public DiamondFlowersItem(int exp, double dropChance, int maxAmount, int minAmount, Material material){
	  this.exp = exp;
	  this.dropChance = dropChance;
	  this.maxAmount = maxAmount;
	  this.minAmount = minAmount;
	  this.material = material;
	}
  }
}
