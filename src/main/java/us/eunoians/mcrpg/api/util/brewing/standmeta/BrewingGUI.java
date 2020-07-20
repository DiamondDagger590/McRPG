package us.eunoians.mcrpg.api.util.brewing.standmeta;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.sorcery.CircesRecipes;
import us.eunoians.mcrpg.abilities.sorcery.HastyBrew;
import us.eunoians.mcrpg.abilities.sorcery.ManaAffinity;
import us.eunoians.mcrpg.api.events.mcrpg.sorcery.HastyBrewEvent;
import us.eunoians.mcrpg.api.events.mcrpg.sorcery.ManaAffinityEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.artifacts.ArtifactFactory;
import us.eunoians.mcrpg.api.util.artifacts.ArtifactManager;
import us.eunoians.mcrpg.api.util.books.BookManager;
import us.eunoians.mcrpg.api.util.books.SkillBookFactory;
import us.eunoians.mcrpg.api.util.brewing.BasePotion;
import us.eunoians.mcrpg.api.util.brewing.BrewingStandManager;
import us.eunoians.mcrpg.api.util.brewing.PotionEffectTagWrapper;
import us.eunoians.mcrpg.api.util.brewing.PotionFactory;
import us.eunoians.mcrpg.api.util.brewing.PotionRecipeManager;
import us.eunoians.mcrpg.api.util.brewing.TagMeta;
import us.eunoians.mcrpg.gui.GUI;
import us.eunoians.mcrpg.gui.GUIBuilder;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.BasePotionType;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.Parser;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BrewingGUI extends GUI{
  
  @Getter
  @Setter
  private ItemStack fuel;
  
  @Getter
  private BrewingStand holder;
  
  @Getter
  private ItemStack ingredient;
  
  @Getter
  @Setter
  private int currentFuelLevel;
  
  @Getter
  @Setter
  private int maxCurrentFuelLevel = 0;
  
  @Getter
  @Setter
  private double currentBrewProgress;
  
  @Getter
  private double potionBrewDuration = 10;
  
  @Getter
  private BukkitTask brewTask;
  
  @Getter
  private Inventory inv;
  
  private ItemStack[] specialRewards = new ItemStack[4];
  
  private BasePotion[] potionItems = new BasePotion[3];
  
  private final int VANILLA_FUEL_SLOT = 4;
  private final int VANILLA_INGREDIENT_SLOT = 3;
  private final int VANILLA_POTION_SLOT_1 = 0;
  private final int VANILLA_POTION_SLOT_2 = 1;
  private final int VANILLA_POTION_SLOT_3 = 2;
  private final int MCRPG_POTION_SLOT_1 = 38;
  private final int MCRPG_POTION_SLOT_2 = 40;
  private final int MCRPG_POTION_SLOT_3 = 42;
  private final int MCRPG_INGREDIENT_SLOT = 13;
  private final int MCRPG_SPECIAL_ITEM_SLOT_1 = 46;
  private final int MCRPG_SPECIAL_ITEM_SLOT_2 = 48;
  private final int MCRPG_SPECIAL_ITEM_SLOT_3 = 50;
  private final int MCRPG_SPECIAL_ITEM_SLOT_4 = 52;
  
  @Getter
  @Setter
  private OfflinePlayer lastInteractedPlayer;
  @Getter
  @Setter
  private OfflinePlayer playerStartingBrew;
  
  public BrewingGUI(BrewingStand brewingStand, FileConfiguration storage){
    super(new GUIBuilder());
    holder = brewingStand;
    
    String key = "BrewingStands." + Methods.locToString(brewingStand.getLocation()) + ".";
    FileConfiguration guiFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_GUI);
    FileConfiguration potionConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_ITEMS_CONFIG);
    potionBrewDuration = potionConfig.getDouble("Potions.PotionBrewDuration", 10d);
    inv = Bukkit.createInventory(null, 54, Methods.color(guiFile.getString("Title")));
    BrewerInventory brewerInventory = brewingStand.getInventory();
    //if(created){
    if((brewerInventory.getFuel() == null || brewerInventory.getFuel().getType() == Material.AIR) && !storage.contains(key + "FuelItem")){
      //Init the fuel item
      fuel = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
      ItemMeta fuelMeta = fuel.getItemMeta();
      fuelMeta.setDisplayName(Methods.color(guiFile.getString("FuelPlaceholder.DisplayName")));
      fuelMeta.setLore(Methods.colorLore(guiFile.getStringList("FuelPlaceholder.Lore")));
      fuel.setItemMeta(fuelMeta);
      inv.setItem(0, fuel);
      
    }
    else{
      fuel = storage.contains(key + "FuelItem") ? storage.getItemStack(key + "FuelItem") : brewerInventory.getFuel();
      inv.setItem(0, fuel);
    }
    currentFuelLevel = storage.getInt(key + "CurrentFuelLevel", 0);
    maxCurrentFuelLevel = storage.getInt(key + "MaxFuelLevel", 0);
    brewingStand.setFuelLevel(0);
    brewingStand.getSnapshotInventory().setItem(VANILLA_FUEL_SLOT, new ItemStack(Material.AIR));
    //Fuel glass
    initFuelItems();
    updateFuelItems();
    
    if(brewerInventory.getIngredient() == null || brewerInventory.getIngredient().getType() == Material.AIR){
      //set ingredient
      ingredient = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
      ItemMeta ingredientMeta = ingredient.getItemMeta();
      ingredientMeta.setDisplayName(Methods.color(guiFile.getString("IngredientPlaceholder.DisplayName")));
      ingredientMeta.setLore(Methods.colorLore(guiFile.getStringList("IngredientPlaceholder.Lore")));
      ingredient.setItemMeta(ingredientMeta);
      inv.setItem(MCRPG_INGREDIENT_SLOT, ingredient);
    }
    else{
      ingredient = brewerInventory.getIngredient();
      inv.setItem(MCRPG_INGREDIENT_SLOT, ingredient);
    }
    
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
    if(brewerInventory.getItem(VANILLA_POTION_SLOT_1) == null || brewerInventory.getItem(VANILLA_POTION_SLOT_1).getType() == Material.AIR
    || !(brewerInventory.getItem(VANILLA_POTION_SLOT_1).getType().name().contains("POTION"))){
      inv.setItem(MCRPG_POTION_SLOT_1, potionGlass);
    }
    else{
      BasePotion basePotion = PotionFactory.convertItemStackToBasePotion(brewerInventory.getItem(VANILLA_POTION_SLOT_1));
      inv.setItem(MCRPG_POTION_SLOT_1, basePotion.getAsItem());
      brewerInventory.setItem(VANILLA_POTION_SLOT_1, basePotion.getAsItem());
      potionItems[0] = basePotion;
    }
    if(brewerInventory.getItem(VANILLA_POTION_SLOT_2) == null || brewerInventory.getItem(VANILLA_POTION_SLOT_2).getType() == Material.AIR
         || !(brewerInventory.getItem(VANILLA_POTION_SLOT_2).getType().name().contains("POTION"))){
      inv.setItem(MCRPG_POTION_SLOT_2, potionGlass);
    }
    else{
      BasePotion basePotion = PotionFactory.convertItemStackToBasePotion(brewerInventory.getItem(VANILLA_POTION_SLOT_2));
      inv.setItem(MCRPG_POTION_SLOT_2, basePotion.getAsItem());
      brewerInventory.setItem(VANILLA_POTION_SLOT_2, basePotion.getAsItem());
      potionItems[1] = basePotion;
    }
    if(brewerInventory.getItem(VANILLA_POTION_SLOT_3) == null || brewerInventory.getItem(VANILLA_POTION_SLOT_3).getType() == Material.AIR
         || !(brewerInventory.getItem(VANILLA_POTION_SLOT_3).getType().name().contains("POTION"))){
      inv.setItem(MCRPG_POTION_SLOT_3, potionGlass);
    }
    else{
      BasePotion basePotion = PotionFactory.convertItemStackToBasePotion(brewerInventory.getItem(VANILLA_POTION_SLOT_3));
      inv.setItem(MCRPG_POTION_SLOT_3, basePotion.getAsItem());
      brewerInventory.setItem(VANILLA_POTION_SLOT_3, basePotion.getAsItem());
      potionItems[2] = basePotion;
    }
    
    initProgressBar();
    
    ItemStack specialItemGlass = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
    ItemMeta specialItemMeta = specialItemGlass.getItemMeta();
    specialItemMeta.setDisplayName(Methods.color(guiFile.getString("SpecialItemPlaceholder.DisplayName")));
    specialItemMeta.setLore(Methods.colorLore(guiFile.getStringList("SpecialItemPlaceholder.Lore")));
    specialItemGlass.setItemMeta(specialItemMeta);
    for(int i = 0; i < 4; i++){
      int index = 46 + (i * 2);
      ItemStack itemToSet = null;
      if(storage.contains(key + "BonusItems." + i)){
        specialRewards[i] = storage.getItemStack(key + "BonusItems." + (i));
        itemToSet = specialRewards[i];
      }
      inv.setItem(index, itemToSet == null ? specialItemGlass : itemToSet);
    }
    
    if(!guiFile.getBoolean("RemoveFillerGlass", false)){
      ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta placeholderMeta = placeholder.getItemMeta();
      placeholderMeta.setDisplayName(" ");
      placeholder.setItemMeta(placeholderMeta);
  
      for(int i = 0; i < inv.getSize(); i++){
        if(inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR){
          inv.setItem(i, placeholder);
        }
      }
    }
    getGui().setNewInventory(inv);
  }
  
  public List<ItemStack> getSpecialRewards(){
    return Arrays.asList(specialRewards);
  }
  
  public List<BasePotion> getPotionItems(){
    return Arrays.asList(potionItems);
  }
  
  public void breakGUI(){
    for(HumanEntity humanEntity : inv.getViewers()){
      humanEntity.closeInventory();
    }
    Location dropLocation = holder.getLocation();
    /*
    for(BasePotion basePotion : potionItems){
      if(basePotion != null){
        dropLocation.getWorld().dropItemNaturally(dropLocation, basePotion.getAsItem());
      }
    }
    for(ItemStack specialItem : specialRewards){
      if(specialItem != null){
        dropLocation.getWorld().dropItemNaturally(dropLocation, specialItem);
      }
    }
    if(ingredient != null && ingredient.getType() != Material.AIR){
      dropLocation.getWorld().dropItemNaturally(dropLocation, ingredient);
    }*/
    if(holder.getInventory().getIngredient() != null && holder.getInventory().getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE){
      updateHolder();
      holder.getInventory().setIngredient(new ItemStack(Material.AIR));
      holder.update();
    }
    if(fuel != null && fuel.getType() != Material.AIR && fuel.getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE){
      dropLocation.getWorld().dropItemNaturally(dropLocation, fuel);
    }
    for(ItemStack itemStack : specialRewards){
      if(itemStack != null){
        dropLocation.getWorld().dropItemNaturally(dropLocation, itemStack);
      }
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
      glassMeta.setDisplayName(Methods.color("&c" + format.format(percent) + "%"));
      glass.setItemMeta(glassMeta);
      inv.setItem(i * 9, glass);
    }
  }
  
  //TODO come back and optimize this. No need to update all five glass each time
  public void updateFuelItems(){
    updateHolder();
    inv.setItem(0, fuel);
    if(currentFuelLevel == 0){
      if(fuel.getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE){
        currentFuelLevel = McRPG.getInstance().getPotionRecipeManager().getFuelAmount(fuel);
        maxCurrentFuelLevel = currentFuelLevel;
        if(fuel.getAmount() == 1){
          FileConfiguration guiFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_GUI);
          fuel = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
          ItemMeta fuelMeta = fuel.getItemMeta();
          fuelMeta.setDisplayName(Methods.color(guiFile.getString("FuelPlaceholder.DisplayName")));
          fuelMeta.setLore(Methods.colorLore(guiFile.getStringList("FuelPlaceholder.Lore")));
          fuel.setItemMeta(fuelMeta);
          inv.setItem(0, fuel);
        }
        else{
          fuel.setAmount(fuel.getAmount() - 1);
          currentFuelLevel = McRPG.getInstance().getPotionRecipeManager().getFuelAmount(fuel);
          maxCurrentFuelLevel = currentFuelLevel;
          inv.setItem(0, fuel);
        }
      }
    }
    double numerator = 20 * currentFuelLevel;
    int usedGlassPanes = (int) (numerator / (maxCurrentFuelLevel != 0 ? maxCurrentFuelLevel : 1));
    int glassToPopulate = numerator != 0 ? usedGlassPanes : 0;
    double percent = 100 * ((double) currentFuelLevel / (double) (maxCurrentFuelLevel != 0 ? maxCurrentFuelLevel : 1));
    for(int i = 5; i >= 1; i--){
      int fuelForGlass;
      if(glassToPopulate >= 4){
        fuelForGlass = 4;
        glassToPopulate -= 4;
      }
      else{
        fuelForGlass = glassToPopulate;
        glassToPopulate = 0;
      }
      ItemStack glass = inv.getItem(i * 9);
      glass.setType(getFuelGlass(fuelForGlass));
      ItemMeta glassMeta = glass.getItemMeta();
      DecimalFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(2);
      format.setMaximumFractionDigits(2);
      glassMeta.setDisplayName(Methods.color(getFuelGlassColour(fuelForGlass) + format.format(percent) + "%"));
      glass.setItemMeta(glassMeta);
      inv.setItem(i * 9, glass);
    }
    save();
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
    }
  }
  
  public void resetFuelGlass(){
    FileConfiguration guiFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_GUI);
    fuel = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    ItemMeta fuelMeta = fuel.getItemMeta();
    fuelMeta.setDisplayName(Methods.color(guiFile.getString("FuelPlaceholder.DisplayName")));
    fuelMeta.setLore(Methods.colorLore(guiFile.getStringList("FuelPlaceholder.Lore")));
    fuel.setItemMeta(fuelMeta);
    inv.setItem(0, fuel);
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
    }
  }
  
  public void resetIngredientGlass(){
    updateHolder();
    FileConfiguration guiFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_GUI);
    ingredient = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    ItemMeta ingredientMeta = ingredient.getItemMeta();
    ingredientMeta.setDisplayName(Methods.color(guiFile.getString("IngredientPlaceholder.DisplayName")));
    ingredientMeta.setLore(Methods.colorLore(guiFile.getStringList("IngredientPlaceholder.Lore")));
    ingredient.setItemMeta(ingredientMeta);
    inv.setItem(MCRPG_INGREDIENT_SLOT, ingredient);
    holder.getSnapshotInventory().setItem(VANILLA_INGREDIENT_SLOT, new ItemStack(Material.AIR));
    holder.update(true, true);
    cancelBrewTask();
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
    }
  }
  
  public void resetSpecialItemGlass(){
    FileConfiguration guiFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_GUI);
    ItemStack specialItemGlass = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
    ItemMeta specialItemMeta = specialItemGlass.getItemMeta();
    specialItemMeta.setDisplayName(Methods.color(guiFile.getString("SpecialItemPlaceholder.DisplayName")));
    specialItemMeta.setLore(Methods.colorLore(guiFile.getStringList("SpecialItemPlaceholder.Lore")));
    specialItemGlass.setItemMeta(specialItemMeta);
    for(int i = 0; i < 4; i++){
      int index = 46 + (i * 2);
      inv.setItem(index, specialRewards[i] == null ? specialItemGlass : specialRewards[i]);
    }
  }
  
  public void resetPotionGlass(){
    FileConfiguration guiFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_GUI);
    ItemStack potionGlass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    ItemMeta potionMeta = potionGlass.getItemMeta();
    potionMeta.setDisplayName(Methods.color(guiFile.getString("PotionPlaceholder.DisplayName")));
    potionMeta.setLore(Methods.colorLore(guiFile.getStringList("PotionPlaceholder.Lore")));
    potionGlass.setItemMeta(potionMeta);
    if(potionItems[0] == null){
      inv.setItem(MCRPG_POTION_SLOT_1, potionGlass);
    }
    if(potionItems[1] == null){
      inv.setItem(MCRPG_POTION_SLOT_2, potionGlass);
    }
    if(potionItems[2] == null){
      inv.setItem(MCRPG_POTION_SLOT_3, potionGlass);
    }
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
    }
  }
  
  public int getFirstEmptySlot(){
    return potionItems[0] == null ? MCRPG_POTION_SLOT_1 : potionItems[1] == null ? MCRPG_POTION_SLOT_2 : potionItems[2] == null ? MCRPG_POTION_SLOT_3 : -1;
  }
  
  public boolean isPotionSlot(int slot){
    return slot == MCRPG_POTION_SLOT_1 || slot == MCRPG_POTION_SLOT_2 || slot == MCRPG_POTION_SLOT_3;
  }
  
  public boolean isSpecialItemSlot(int slot){
    return slot == MCRPG_SPECIAL_ITEM_SLOT_1 || slot == MCRPG_SPECIAL_ITEM_SLOT_2 || slot == MCRPG_SPECIAL_ITEM_SLOT_3 || slot == MCRPG_SPECIAL_ITEM_SLOT_4;
  }
  
  public ItemStack getSpecialItem(int slot){
    if(slot == MCRPG_SPECIAL_ITEM_SLOT_1){
      return specialRewards[0];
    }
    else if(slot == MCRPG_SPECIAL_ITEM_SLOT_2){
      return specialRewards[1];
    }
    if(slot == MCRPG_SPECIAL_ITEM_SLOT_3){
      return specialRewards[2];
    }
    if(slot == MCRPG_SPECIAL_ITEM_SLOT_4){
      return specialRewards[3];
    }
    return null;
  }
  
  public boolean hasSpecialItem(){
    for(ItemStack item : specialRewards){
      if(item != null){
        return true;
      }
    }
    return false;
  }
  
  public boolean specialItemSlotsFull(){
    for(ItemStack item : specialRewards){
      if(item == null){
        return false;
      }
    }
    return true;
  }
  
  public int getFirstEmptySpecialItemSlot(){
    if(specialRewards[0] == null){
      return MCRPG_SPECIAL_ITEM_SLOT_1;
    }
    else if(specialRewards[1] == null){
      return MCRPG_SPECIAL_ITEM_SLOT_2;
    }
    else if(specialRewards[2] == null){
      return MCRPG_SPECIAL_ITEM_SLOT_3;
    }
    else if(specialRewards[3] == null){
      return MCRPG_SPECIAL_ITEM_SLOT_4;
    }
    return 0;
  }
  
  public void updateIngredient(){
    updateHolder();
    if(ingredient.getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE){
      holder.getSnapshotInventory().setIngredient(ingredient);
      holder.update();
    }
    inv.setItem(MCRPG_INGREDIENT_SLOT, ingredient);
  }
  
  public void setPotion(ItemStack potion, int slot){
    updateHolder();
    BasePotion basePotion = PotionFactory.convertItemStackToBasePotion(potion);
    if(slot == MCRPG_POTION_SLOT_1){
      potionItems[0] = basePotion;
      inv.setItem(MCRPG_POTION_SLOT_1, basePotion.getAsItem());
      holder.getSnapshotInventory().setItem(VANILLA_POTION_SLOT_1, basePotion.getAsItem());
    }
    else if(slot == MCRPG_POTION_SLOT_2){
      potionItems[1] = basePotion;
      inv.setItem(MCRPG_POTION_SLOT_2, basePotion.getAsItem());
      holder.getSnapshotInventory().setItem(VANILLA_POTION_SLOT_2, basePotion.getAsItem());
    }
    else if(slot == MCRPG_POTION_SLOT_3){
      potionItems[2] = basePotion;
      inv.setItem(MCRPG_POTION_SLOT_3, basePotion.getAsItem());
      holder.getSnapshotInventory().setItem(VANILLA_POTION_SLOT_3, basePotion.getAsItem());
    }
    holder.update(true, true);
  }
  
  public void removeSpecialReward(int slot){
    if(slot == MCRPG_SPECIAL_ITEM_SLOT_1){
      specialRewards[0] = null;
    }
    else if(slot == MCRPG_SPECIAL_ITEM_SLOT_2){
      specialRewards[1] = null;
    }
    else if(slot == MCRPG_SPECIAL_ITEM_SLOT_3){
      specialRewards[2] = null;
    }
    else if(slot == MCRPG_SPECIAL_ITEM_SLOT_4){
      specialRewards[3] = null;
    }
    resetSpecialItemGlass();
    save();
  }
  
  public void removePotion(int slot){
    updateHolder();
    if(slot == MCRPG_POTION_SLOT_1){
      potionItems[0] = null;
      inv.setItem(MCRPG_POTION_SLOT_1, null);
      holder.getSnapshotInventory().setItem(VANILLA_POTION_SLOT_1, new ItemStack(Material.AIR));
    }
    else if(slot == MCRPG_POTION_SLOT_2){
      potionItems[1] = null;
      inv.setItem(MCRPG_POTION_SLOT_2, null);
      holder.getSnapshotInventory().setItem(VANILLA_POTION_SLOT_2, new ItemStack(Material.AIR));
    }
    else if(slot == MCRPG_POTION_SLOT_3){
      potionItems[2] = null;
      inv.setItem(MCRPG_POTION_SLOT_3, null);
      holder.getSnapshotInventory().setItem(VANILLA_POTION_SLOT_3, new ItemStack(Material.AIR));
    }
    holder.update(true, true);
    resetPotionGlass();
    for(BasePotion basePotion : potionItems){
      if(basePotion != null){
        return;
      }
    }
    //If return wasn't fired from the loop, then we can assume we need to cancel
    cancelBrewTask();
  }
  
  private void updateHolder(){
    holder = (BrewingStand) holder.getWorld().getBlockAt(holder.getLocation()).getState();
  }
  
  public ItemStack getPotion(int slot){
    return inv.getItem(slot);
  }
  
  public boolean checkForBrewingTask(){
    return currentFuelLevel > 0 && isIngredientValid(ingredient) && brewTask == null;
  }
  
  public BukkitTask startBrewTask(){
    updateHolder();
    double delay = (getBrewDuration()) / 24d;
    
    BrewingGUI gui = this;
    BukkitTask task = new BukkitRunnable(){
      @Override
      public void run(){
        if(currentBrewProgress >= getBrewDuration()){
          cancel();
          finishBrewTask();
          return;
        }
        gui.setCurrentBrewProgress(gui.getCurrentBrewProgress() + delay);
        updateProgressBar();
      }
    }.runTaskTimer(McRPG.getInstance(), (long) (delay * 20), (long) (delay * 20));
    this.brewTask = task;
    return task;
  }
  
  public void cancelBrewTask(){
    if(brewTask != null){
      brewTask.cancel();
    }
    brewTask = null;
    currentBrewProgress = 0;
    initProgressBar();
  }
  
  public void finishBrewTask(){
    updateHolder();
    Chunk chunk = holder.getChunk();
    boolean loadedChunk = false;
    PotionRecipeManager potionRecipeManager = McRPG.getInstance().getPotionRecipeManager();
    BrewerInventory snapshotInventory = holder.getSnapshotInventory();
    if(!chunk.isLoaded()){
      //TODO
    }
    BrewEvent brewEvent = new BrewEvent(holder.getBlock(), snapshotInventory, currentFuelLevel);
    Bukkit.getPluginManager().callEvent(brewEvent);
    if(!brewEvent.isCancelled()){
  
      OfflinePlayer offlinePlayer = getPlayerStartingBrew();
      if(offlinePlayer != null && offlinePlayer.isOnline()){
        Advancement advancement = Bukkit.getServer().getAdvancement(NamespacedKey.minecraft("nether/brew_potion"));
        if(advancement != null){
          AdvancementProgress advancementProgress = ((Player) offlinePlayer).getAdvancementProgress(advancement);
          if(!advancementProgress.isDone()){
            for(String criteria : advancementProgress.getRemainingCriteria()){
              advancementProgress.awardCriteria(criteria);
            }
          }
        }
      }
      
      FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
      holder.getLocation().getWorld().playSound(holder.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Brewing.FinishBrewSound.Sound")),
        Float.parseFloat(soundFile.getString("Sounds.Brewing.FinishBrewSound.Volume")), Float.parseFloat(soundFile.getString("Sounds.Brewing.FinishBrewSound.Pitch")));
      int expToAward = 0;
      FileConfiguration sorceryFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
      String expKey = "ExpAwardedPerBrewAmount.";
      for(int i = 0; i < potionItems.length; i++){
        BasePotion basePotion = potionItems[i];
        if(basePotion != null){
          PotionEffectTagWrapper potionEffectTagWrapper = potionRecipeManager.getPotionEffectTagWrapper(basePotion.getBasePotionType());
          if(basePotion.getAsItem().getType() == Material.POTION && ingredient.getType() == Material.GUNPOWDER && potionEffectTagWrapper.isCanBeSplash()){
            int preAmount = basePotion.getTotalTimesModified();
            basePotion.setSplash();
            int postAmount = basePotion.getTotalTimesModified();
            if(preAmount != postAmount){
              expToAward += sorceryFile.getInt(expKey + postAmount);
            }
            potionItems[i] = basePotion;
            continue;
          }
          if(basePotion.getAsItem().getType() == Material.SPLASH_POTION && ingredient.getType() == Material.DRAGON_BREATH && potionEffectTagWrapper.isCanBeLingering()){
            int preAmount = basePotion.getTotalTimesModified();
            basePotion.setLingering();
            int postAmount = basePotion.getTotalTimesModified();
            if(preAmount != postAmount){
              expToAward += sorceryFile.getInt(expKey + postAmount);
            }
            potionItems[i] = basePotion;
            continue;
          }
          TagMeta tagMeta = potionEffectTagWrapper.getTagMeta(basePotion.getTag());
          String newTag = potionEffectTagWrapper.getTagMeta(basePotion.getTag()).getChildTag(ingredient.getType());
          if(newTag != null){
            String[] tagData = newTag.split("\\.");
            if(tagData.length > 1){
              BasePotionType basePotionType;
              if(tagData[0].equals("AWKWARD")){
                basePotionType = BasePotionType.AWKWARD;
              }
              else{
                basePotionType = BasePotionType.getFromPotionEffect(PotionEffectTypeWrapper.getByName(tagData[0]));
              }
              basePotion.setBasePotionType(basePotionType);
              newTag = tagData[1];
            }
            basePotion.setTag(newTag);
            int preAmount = basePotion.getTotalTimesModified();
            basePotion.updateInfo();
            int postAmount = basePotion.getTotalTimesModified();
            if(preAmount != postAmount){
              expToAward += sorceryFile.getInt(expKey + postAmount);
            }
            potionItems[i] = basePotion;
          }
        }
      }
      if(lastInteractedPlayer != null && lastInteractedPlayer.isOnline()){
        try{
          McRPGPlayer mp = PlayerManager.getPlayer(lastInteractedPlayer.getUniqueId());
          mp.giveExp(Skills.SORCERY, expToAward, GainReason.BREW);
        }catch(McRPGPlayerNotFoundException e){
          e.printStackTrace();
        }
      }
      if(potionItems[0] != null){
        snapshotInventory.setItem(VANILLA_POTION_SLOT_1, potionItems[0].getAsItem());
        inv.setItem(MCRPG_POTION_SLOT_1, potionItems[0].getAsItem());
        holder.update(true, true);
      }
      if(potionItems[1] != null){
        snapshotInventory.setItem(VANILLA_POTION_SLOT_2, potionItems[1].getAsItem());
        inv.setItem(MCRPG_POTION_SLOT_2, potionItems[1].getAsItem());
        holder.update(true, true);
      }
      if(potionItems[2] != null){
        snapshotInventory.setItem(VANILLA_POTION_SLOT_3, potionItems[2].getAsItem());
        inv.setItem(MCRPG_POTION_SLOT_3, potionItems[2].getAsItem());
        holder.update(true, true);
      }
      currentFuelLevel--;
      if(lastInteractedPlayer != null && lastInteractedPlayer.isOnline()){
        McRPGPlayer mp = null;
        try{
          mp = PlayerManager.getPlayer(lastInteractedPlayer.getUniqueId());
        }catch(McRPGPlayerNotFoundException e){
        }
        if(mp != null){
          BookManager bookManager = McRPG.getInstance().getBookManager();
          ArtifactManager artifactManager = McRPG.getInstance().getArtifactManager();
          Random rand = new Random();
          if(!specialItemSlotsFull() && bookManager.getEnabledUnlockEvents().contains("Brewing")){
            int bookChance = McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getBoolean("Configuration.DisableBooksInEnd", false) && holder.getLocation().getBlock().getBiome().name().contains("END") ? 100001 : rand.nextInt(100000);
            double chance = bookManager.getDefaultUnlockChance();
            if(sorceryFile.getBoolean("SorceryEnabled") && UnlockedAbilities.MANA_AFFINITY.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.MANA_AFFINITY)
                 && mp.getBaseAbility(UnlockedAbilities.MANA_AFFINITY).isToggled()){
              ManaAffinity manaAffinity = (ManaAffinity) mp.getBaseAbility(UnlockedAbilities.MANA_AFFINITY);
              double discoveryChanceIncrease = sorceryFile.getDouble("ManaAffinityConfig.Tier" + Methods.convertToNumeral(manaAffinity.getCurrentTier()) + ".DiscoveryChanceIncrease");
              ManaAffinityEvent manaAffinityEvent = new ManaAffinityEvent(mp, manaAffinity, discoveryChanceIncrease);
              Bukkit.getPluginManager().callEvent(manaAffinityEvent);
              if(!manaAffinityEvent.isCancelled()){
                chance += manaAffinityEvent.getDiscoveryChanceIncrease();
              }
            }
            chance *= 1000;
            if(chance >= bookChance){
              int slot = getFirstEmptySpecialItemSlot();
              if(slot != 0){
                ItemStack book = SkillBookFactory.generateUnlockBook();
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_1){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_1, book);
                  specialRewards[0] = book;
                }
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_2){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_2, book);
                  specialRewards[1] = book;
                }
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_3){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_3, book);
                  specialRewards[2] = book;
                }
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_4){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_4, book);
                  specialRewards[3] = book;
                }
              }
            }
          }
          if(!specialItemSlotsFull() && bookManager.getEnabledUpgradeEvents().contains("Brewing")){
            int bookChance = McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getBoolean("Configuration.DisableBooksInEnd", false) && holder.getLocation().getBlock().getBiome().name().contains("END") ? 100001 : rand.nextInt(100000);
            double chance = bookManager.getDefaultUpgradeChance();
            if(sorceryFile.getBoolean("SorceryEnabled") && UnlockedAbilities.MANA_AFFINITY.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.MANA_AFFINITY)
                 && mp.getBaseAbility(UnlockedAbilities.MANA_AFFINITY).isToggled()){
              ManaAffinity manaAffinity = (ManaAffinity) mp.getBaseAbility(UnlockedAbilities.MANA_AFFINITY);
              double discoveryChanceIncrease = sorceryFile.getDouble("ManaAffinityConfig.Tier" + Methods.convertToNumeral(manaAffinity.getCurrentTier()) + ".DiscoveryChanceIncrease");
              ManaAffinityEvent manaAffinityEvent = new ManaAffinityEvent(mp, manaAffinity, discoveryChanceIncrease);
              Bukkit.getPluginManager().callEvent(manaAffinityEvent);
              if(!manaAffinityEvent.isCancelled()){
                chance += manaAffinityEvent.getDiscoveryChanceIncrease();
              }
            }
            chance *= 1000;
            if(chance >= bookChance){
              int slot = getFirstEmptySpecialItemSlot();
              if(slot != 0){
                ItemStack book = SkillBookFactory.generateUpgradeBook();
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_1){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_1, book);
                  specialRewards[0] = book;
                }
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_2){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_2, book);
                  specialRewards[1] = book;
                }
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_3){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_3, book);
                  specialRewards[2] = book;
                }
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_4){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_4, book);
                  specialRewards[3] = book;
                }
              }
            }
          }
          if(!specialItemSlotsFull()){
            double chance = artifactManager.getArtifactTypeChance("BrewingArtifacts");
            if(sorceryFile.getBoolean("SorceryEnabled") && UnlockedAbilities.MANA_AFFINITY.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.MANA_AFFINITY)
                 && mp.getBaseAbility(UnlockedAbilities.MANA_AFFINITY).isToggled()){
              ManaAffinity manaAffinity = (ManaAffinity) mp.getBaseAbility(UnlockedAbilities.MANA_AFFINITY);
              double discoveryChanceIncrease = sorceryFile.getDouble("ManaAffinityConfig.Tier" + Methods.convertToNumeral(manaAffinity.getCurrentTier()) + ".DiscoveryChanceIncrease");
              ManaAffinityEvent manaAffinityEvent = new ManaAffinityEvent(mp, manaAffinity, discoveryChanceIncrease);
              Bukkit.getPluginManager().callEvent(manaAffinityEvent);
              if(!manaAffinityEvent.isCancelled()){
                chance += manaAffinityEvent.getDiscoveryChanceIncrease();
              }
            }
            chance *= 1000;
            if(chance >= rand.nextInt(100000)){
              int slot = getFirstEmptySpecialItemSlot();
              if(slot != 0){
                ItemStack artifact = ArtifactFactory.generateArtifact("BrewingArtifacts");
                NBTItem nbtItem = new NBTItem(artifact);
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_1){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_1, artifact);
                  specialRewards[0] = artifact;
                }
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_2){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_2, artifact);
                  specialRewards[1] = artifact;
                }
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_3){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_3, artifact);
                  specialRewards[2] = artifact;
                }
                if(slot == MCRPG_SPECIAL_ITEM_SLOT_4){
                  inv.setItem(MCRPG_SPECIAL_ITEM_SLOT_4, artifact);
                  specialRewards[3] = artifact;
                }
              }
            }
          }
        }
      }
    }
    if(ingredient.getAmount() == 1){
      resetIngredientGlass();
    }
    else{
      ingredient.setAmount(ingredient.getAmount() - 1);
    }
    updateFuelItems();
    updateIngredient();
    currentBrewProgress = 0;
    initProgressBar();
    this.brewTask = null;
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
    }
  }
  
  private boolean isIngredientValid(ItemStack ingredient){
    Material ingredientType = ingredient.getType();
    PotionRecipeManager potionRecipeManager = McRPG.getInstance().getPotionRecipeManager();
    for(BasePotion basePotion : potionItems){
      if(basePotion != null){
        PotionEffectTagWrapper potionEffectTagWrapper = potionRecipeManager.getPotionEffectTagWrapper(basePotion.getBasePotionType());
        if(basePotion.getAsItem().getType() == Material.POTION && ingredient.getType() == Material.GUNPOWDER && potionEffectTagWrapper.isCanBeSplash()){
          return true;
        }
        else if(basePotion.getAsItem().getType() == Material.SPLASH_POTION && ingredient.getType() == Material.DRAGON_BREATH && potionEffectTagWrapper.isCanBeLingering()){
          return true;
        }
        else if(potionRecipeManager.doesMaterialLeadToChild(ingredientType, basePotion)){
          BasePotionType basePotionType = potionRecipeManager.getChildPotionType(ingredientType, basePotion);
          if(potionRecipeManager.isLockedRecipe(basePotionType)){
            if(lastInteractedPlayer != null && lastInteractedPlayer.isOnline()){
              try{
                FileConfiguration sorceryConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
                McRPGPlayer mp = PlayerManager.getPlayer(lastInteractedPlayer.getUniqueId());
                if(sorceryConfig.getBoolean("SorceryEnabled") && UnlockedAbilities.CIRCES_RECIPES.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.CIRCES_RECIPES)
                     && mp.getBaseAbility(UnlockedAbilities.CIRCES_RECIPES).isToggled()){
                  CircesRecipes circesRecipes = (CircesRecipes) mp.getBaseAbility(UnlockedAbilities.CIRCES_RECIPES);
                  return potionRecipeManager.getTypesForTier(circesRecipes.getCurrentTier()).contains(basePotionType);
                }
              }catch(McRPGPlayerNotFoundException e){
                continue;
              }
              continue;
            }
            else{
              continue;
            }
          }
          return true;
        }
      }
    }
    return false;
  }
  
  public void setIngredient(ItemStack ingredient){
    updateHolder();
    this.ingredient = ingredient;
    inv.setItem(MCRPG_INGREDIENT_SLOT, ingredient);
    holder.getSnapshotInventory().setIngredient(ingredient);
    holder.update(true, true);
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
    }
  }
  
  private void initProgressBar(){
    for(int i = 6; i >= 1; i--){
      ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
      ItemMeta glassMeta = glass.getItemMeta();
      double percent = (double) currentBrewProgress / (double) getBrewDuration();
      DecimalFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(2);
      format.setMaximumFractionDigits(2);
      glassMeta.setDisplayName(Methods.color("&c" + format.format(percent) + "%"));
      glass.setItemMeta(glassMeta);
      inv.setItem((i * 9) - 1, glass);
    }
  }
  
  //TODO come back and optimize this. Same reason as the fuel items
  private void updateProgressBar(){
    double brewDuration = getBrewDuration();
    double numerator = 24 * brewDuration;
    int usedGlassPanes = (int) (currentBrewProgress / (brewDuration != 0 ? brewDuration : 1));
    int glassToPopulate = (int) ((currentBrewProgress / brewDuration) * 24);
    double percent = 100 * ((double) currentBrewProgress / (double) (brewDuration != 0 ? brewDuration : 1));
    for(int i = 6; i >= 1; i--){
      int potionGlass;
      if(glassToPopulate >= 4){
        potionGlass = 4;
        glassToPopulate -= 4;
      }
      else{
        potionGlass = glassToPopulate;
        glassToPopulate = 0;
      }
      ItemStack glass = inv.getItem((i * 9) - 1);
      glass.setType(getFuelGlass(potionGlass));
      ItemMeta glassMeta = glass.getItemMeta();
      DecimalFormat format = new DecimalFormat();
      format.setMinimumFractionDigits(2);
      format.setMaximumFractionDigits(2);
      glassMeta.setDisplayName(Methods.color(getFuelGlassColour(potionGlass) + format.format(percent <= 100 ? percent : 100) + "%"));
      glass.setItemMeta(glassMeta);
      inv.setItem((i * 9) - 1, glass);
    }
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
    }
  }
  
  private double getBrewDuration(){
    if(lastInteractedPlayer != null && lastInteractedPlayer.isOnline()){
      try{
        McRPGPlayer mp = PlayerManager.getPlayer(lastInteractedPlayer.getUniqueId());
        FileConfiguration sorceryFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
        if(sorceryFile.getBoolean("SorceryEnabled") && DefaultAbilities.HASTY_BREW.isEnabled() && mp.getBaseAbility(DefaultAbilities.HASTY_BREW).isToggled()){
          HastyBrew hastyBrew = (HastyBrew) mp.getBaseAbility(DefaultAbilities.HASTY_BREW);
          Parser parser = DefaultAbilities.HASTY_BREW.getActivationEquation();
          parser.setVariable("sorcery_level", mp.getSkill(Skills.SORCERY).getCurrentLevel());
          parser.setVariable("power_level", mp.getPowerLevel());
          double multiplier = parser.getValue();
          HastyBrewEvent hastyBrewEvent = new HastyBrewEvent(mp, hastyBrew, multiplier);
          Bukkit.getPluginManager().callEvent(hastyBrewEvent);
          if(hastyBrewEvent.isCancelled()){
            return potionBrewDuration;
          }
          multiplier = hastyBrewEvent.getBrewDurationBoost();
          multiplier /= 100;
          multiplier = 1 - multiplier;
          return potionBrewDuration * (multiplier > 0 ? multiplier : 0.2);
        }
      }catch(McRPGPlayerNotFoundException e){
        return potionBrewDuration;
      }
    }
    return potionBrewDuration;
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
    BrewingStandManager brewingStandManager = McRPG.getInstance().getBrewingStandManager();
    if(brewingStandManager.isBrewingStandLoaded(holder)){
      BrewingStandWrapper brewingStandWrapper = brewingStandManager.getBrewingStandWrapper(holder);
      brewingStandWrapper.saveToFile();
    }
  }
}
