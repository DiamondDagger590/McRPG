package us.eunoians.mcrpg.api.util.brewing.standmeta;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.brewing.BasePotion;
import us.eunoians.mcrpg.gui.GUI;
import us.eunoians.mcrpg.gui.GUIBuilder;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

public class BrewingGUI extends GUI {

  @Getter @Setter
  private ItemStack fuel;

  @Getter
  private BrewingStand holder;

  @Getter @Setter
  private ItemStack ingredient;

  @Getter @Setter
  private int currentFuelLevel;

  @Getter @Setter
  private int maxCurrentFuelLevel = 0;

  @Getter @Setter
  private int currentBrewProgress;

  @Getter @Setter
  private int currentMaxBrewProgress = 0;

  @Getter
  private Inventory inv;

  private ItemStack[] specialRewards = new ItemStack[3];

  private BasePotion[] potionItems = new BasePotion[3];

  private List<ItemStack> getSpecialRewards(){
    return Arrays.asList(specialRewards);
  }

  private List<BasePotion> getPotionItems(){
    return Arrays.asList(potionItems);
  }

  private OfflinePlayer lastInteractedPlayer;
  private OfflinePlayer playerStartingBrew;

  public BrewingGUI(BrewingStand brewingStand){
    super(new GUIBuilder());
    holder = brewingStand;
    boolean created = false;
    File folder = new File(McRPG.getInstance().getDataFolder(), File.separator + "brewing_storage");
    if(!folder.exists()){
      folder.mkdirs();
    }
    File storageFile = new File(folder, File.separator + convertLocToFileName(holder.getLocation()));
    if(!storageFile.exists()){
      created = true;
      try{
        storageFile.createNewFile();
      }
      catch(IOException e){
        e.printStackTrace();
      }
    }
    FileConfiguration storage = YamlConfiguration.loadConfiguration(storageFile);
    FileConfiguration guiFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_GUI);
    inv = Bukkit.createInventory(null, 54, Methods.color(guiFile.getString("Title")));
    //if(created){
    if(true){
      //Init the fuel item
      fuel = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
      ItemMeta fuelMeta = fuel.getItemMeta();
      fuelMeta.setDisplayName(Methods.color(guiFile.getString("FuelPlaceholder.DisplayName")));
      fuelMeta.setLore(Methods.colorLore(guiFile.getStringList("FuelPlaceholder.Lore")));
      fuel.setItemMeta(fuelMeta);
      inv.setItem(0, fuel);
      //Fuel glass
      initFuelItems();

      //set ingredient
      ingredient = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
      ItemMeta ingredientMeta = ingredient.getItemMeta();
      ingredientMeta.setDisplayName(Methods.color(guiFile.getString("IngredientPlaceholder.DisplayName")));
      ingredientMeta.setLore(Methods.colorLore(guiFile.getStringList("IngredientPlaceholder.Lore")));
      ingredient.setItemMeta(ingredientMeta);
      inv.setItem(13, ingredient);

      ItemStack tubeGlass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
      ItemMeta tubeMeta = tubeGlass.getItemMeta();
      tubeMeta.setDisplayName(" ");
      tubeGlass.setItemMeta(tubeMeta);
      inv.setItem(21, tubeGlass);
      inv.setItem(22, tubeGlass);
      inv.setItem(23, tubeGlass);
      inv.setItem(29, tubeGlass);
      inv.setItem(31, tubeGlass);
      inv.setItem(33, tubeGlass);

      ItemStack potionGlass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
      ItemMeta potionMeta = potionGlass.getItemMeta();
      potionMeta.setDisplayName(Methods.color(guiFile.getString("PotionPlaceholder.DisplayName")));
      potionMeta.setLore(Methods.colorLore(guiFile.getStringList("PotionPlaceholder.Lore")));
      potionGlass.setItemMeta(potionMeta);
      inv.setItem(38, potionGlass);
      inv.setItem(40, potionGlass);
      inv.setItem(42, potionGlass);

      initProgressBar();

      ItemStack specialItemGlass = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
      ItemMeta specialItemMeta = specialItemGlass.getItemMeta();
      specialItemMeta.setDisplayName(Methods.color(guiFile.getString("SpecialItemPlaceholder.DisplayName")));
      specialItemMeta.setLore(Methods.colorLore(guiFile.getStringList("SpecialItemPlaceholder.Lore")));
      specialItemGlass.setItemMeta(specialItemMeta);
      inv.setItem(46, specialItemGlass);
      inv.setItem(48, specialItemGlass);
      inv.setItem(50, specialItemGlass);
      inv.setItem(52, specialItemGlass);

      ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta placeholderMeta = placeholder.getItemMeta();
      placeholderMeta.setDisplayName(" ");
      placeholder.setItemMeta(placeholderMeta);

      for(int i = 0; i < inv.getSize(); i++){
        if(inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR){
          inv.setItem(i, placeholder);
        }
      }

      getGui().setNewInventory(inv);
    }
  }

  private void initFuelItems(){
    for(int i = 5; i >= 1; i--){
      ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
      ItemMeta glassMeta = glass.getItemMeta();
      double percent = (double) currentFuelLevel / (double) maxCurrentFuelLevel != 0 ? maxCurrentFuelLevel : 1;
      DecimalFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(2);
      format.setMaximumFractionDigits(2);
      glassMeta.setDisplayName(Methods.color("&c"+ format.format(percent) + "%"));
      glass.setItemMeta(glassMeta);
      inv.setItem(i * 9, glass);
    }
  }

  //TODO come back and optimize this. No need to update all five glass each time
  private void updateFuelItems(){
    double numerator = 20 * currentFuelLevel;
    int usedGlassPanes = (int) (numerator / (maxCurrentFuelLevel != 0 ? maxCurrentFuelLevel : 1));
    int glassToPopulate = numerator != 0 ? 20-usedGlassPanes : 0;
    for(int i = 5; i >= 1; i--){
      int fuelForGlass;
      if(glassToPopulate >= 4){
        fuelForGlass = 4;
        glassToPopulate  -= 4;
      }
      else{
        fuelForGlass = glassToPopulate;
        glassToPopulate = 0;
      }
      ItemStack glass = inv.getItem(i * 9);
      glass.setType(getFuelGlass(fuelForGlass));
      ItemMeta glassMeta = glass.getItemMeta();
      double percent = (double) currentFuelLevel / (double) maxCurrentFuelLevel != 0 ? maxCurrentFuelLevel : 1;
      DecimalFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(2);
      format.setMaximumFractionDigits(2);
      glassMeta.setDisplayName(Methods.color(getFuelGlassColour(fuelForGlass) + format.format(percent) + "%"));
      glass.setItemMeta(glassMeta);
      inv.setItem(i * 9, glass);
    }
  }

  private void initProgressBar(){
    for(int i = 6; i >=1; i--){
      ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
      ItemMeta glassMeta = glass.getItemMeta();
      double percent = (double) currentBrewProgress / (double) currentMaxBrewProgress != 0 ? currentMaxBrewProgress : 1;
      DecimalFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(2);
      format.setMaximumFractionDigits(2);
      glassMeta.setDisplayName(Methods.color("&c"+ format.format(percent) + "%"));
      glass.setItemMeta(glassMeta);
      inv.setItem((i * 9)-1, glass);
    }
  }
  //TODO come back and optimize this. Same reason as the fuel items
  private void updateProgressBar(){

  }

  private Material getFuelGlass(int fuelForGlass){
    if(fuelForGlass == 4){
      return Material.LIME_STAINED_GLASS_PANE;
    }
    else if(fuelForGlass == 3){
      return Material.YELLOW_STAINED_GLASS_PANE;
    }
    else if(fuelForGlass == 2){
      return Material.ORANGE_STAINED_GLASS_PANE;
    }
    else{
      return Material.RED_STAINED_GLASS_PANE;
    }
  }

  private String getFuelGlassColour(int fuelForGlass){
    if(fuelForGlass == 4){
      return "&a";
    }
    else if(fuelForGlass == 3){
      return "&e";
    }
    else if(fuelForGlass == 2){
      return "&6";
    }
    else{
      return "&c";
    }
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
    storage.set("FuelLevel", currentFuelLevel);
    storage.set("Fuel", fuel);
    storage.set("Ingredient", ingredient);
    for(int i = 0; i < specialRewards.length; i++){
      if(specialRewards[i] != null){
        storage.set("SpecialItem" + (i+1), specialRewards[i]);
      }
    }
    for(int i = 0; i < potionItems.length; i++){
      if(specialRewards[i] != null){
        storage.set("BrewedPotion" + (i+1), potionItems[i].getAsItem());
      }
    }
  }

  private String convertLocToFileName(Location loc){
    return loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + loc.getWorld().getName();
  }
}
