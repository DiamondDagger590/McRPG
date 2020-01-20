package us.eunoians.mcrpg.api.util.brewing.standmeta;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.brewing.*;
import us.eunoians.mcrpg.gui.GUI;
import us.eunoians.mcrpg.gui.GUIBuilder;
import us.eunoians.mcrpg.types.BasePotionType;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class BrewingGUI extends GUI {

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
  private Inventory inv;

  private ItemStack[] specialRewards = new ItemStack[3];

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
    if(brewerInventory.getItem(VANILLA_POTION_SLOT_1) == null || brewerInventory.getItem(VANILLA_POTION_SLOT_1).getType() == Material.AIR){
      inv.setItem(MCRPG_POTION_SLOT_1, potionGlass);
    }
    else{
      BasePotion basePotion = PotionFactory.convertItemStackToBasePotion(brewerInventory.getItem(VANILLA_POTION_SLOT_1));
      inv.setItem(MCRPG_POTION_SLOT_1, basePotion.getAsItem());
      brewerInventory.setItem(VANILLA_POTION_SLOT_1, basePotion.getAsItem());
      potionItems[0] = basePotion;
    }
    if(brewerInventory.getItem(VANILLA_POTION_SLOT_2) == null || brewerInventory.getItem(VANILLA_POTION_SLOT_2).getType() == Material.AIR){
      inv.setItem(MCRPG_POTION_SLOT_2, potionGlass);
    }
    else{
      BasePotion basePotion = PotionFactory.convertItemStackToBasePotion(brewerInventory.getItem(VANILLA_POTION_SLOT_2));
      inv.setItem(MCRPG_POTION_SLOT_2, basePotion.getAsItem());
      brewerInventory.setItem(VANILLA_POTION_SLOT_2, basePotion.getAsItem());
      potionItems[1] = basePotion;
    }
    if(brewerInventory.getItem(VANILLA_POTION_SLOT_3) == null || brewerInventory.getItem(VANILLA_POTION_SLOT_3).getType() == Material.AIR){
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
      if(storage.contains(key + "BonusItems." + (i + 1))){
        specialRewards[i] = storage.getItemStack(key + "BonusItems." + (i+1));
        itemToSet = specialRewards[i];
      }
      inv.setItem(index, itemToSet == null ? specialItemGlass : itemToSet);
    }

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
    }
    if(fuel != null && fuel.getType() != Material.AIR){
      dropLocation.getWorld().dropItemNaturally(dropLocation, fuel);
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
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
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

  public void updateIngredient(){
    updateHolder();
    holder.getSnapshotInventory().setItem(VANILLA_INGREDIENT_SLOT, ingredient);
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
  }

  private void updateHolder(){
    holder = (BrewingStand) holder.getWorld().getBlockAt(holder.getLocation()).getState();
  }

  public ItemStack getPotion(int slot){
    return inv.getItem(slot);
  }

  public boolean checkForBrewingTask(){
    return currentFuelLevel > 0 && isIngredientValid(ingredient);
  }

  public BukkitTask startBrewTask(){
    updateHolder();
    double delay = (potionBrewDuration)/24d;

    BrewingGUI gui = this;
    BukkitTask task = new BukkitRunnable(){
      @Override
      public void run(){
        if(currentBrewProgress >= potionBrewDuration){
          cancel();
          finishBrewTask();
          return;
        }
        gui.setCurrentBrewProgress(gui.getCurrentBrewProgress() + delay);
        updateProgressBar();
      }
    }.runTaskTimer(McRPG.getInstance(), (long) (delay * 20), (long) (delay * 20));
    return task;
  }

  private void finishBrewTask(){
    updateHolder();
    Chunk chunk = holder.getChunk();
    boolean loadedChunk = false;
    PotionRecipeManager potionRecipeManager = McRPG.getInstance().getPotionRecipeManager();
    BrewerInventory snapshotInventory = holder.getSnapshotInventory();
    if(!chunk.isLoaded()){
      //TODO
    }
    for(int i = 0; i < potionItems.length; i++){
      BasePotion basePotion = potionItems[i];
      if(basePotion != null){
        PotionEffectTagWrapper potionEffectTagWrapper = potionRecipeManager.getPotionEffectTagWrapper(basePotion.getBasePotionType());
        if(basePotion.getAsItem().getType() == Material.POTION && ingredient.getType() == Material.GUNPOWDER && potionEffectTagWrapper.isCanBeSplash()){
          basePotion.setSplash();
          potionItems[i] = basePotion;
          continue;
        }
        if(basePotion.getAsItem().getType() == Material.SPLASH_POTION && ingredient.getType() == Material.DRAGON_BREATH && potionEffectTagWrapper.isCanBeLingering()){
          basePotion.setLingering();
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
          Bukkit.broadcastMessage("Pre update: " + basePotion.getBasePotionType().getName());
          basePotion.setTag(newTag);
          basePotion.updateInfo();
          potionItems[i] = basePotion;
        }
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

    if(ingredient.getAmount() == 1){
      resetIngredientGlass();
    }
    else{
      ingredient.setAmount(ingredient.getAmount() - 1);
    }
    currentFuelLevel--;
    updateFuelItems();
    updateIngredient();
    currentBrewProgress = 0;
    initProgressBar();
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
    }
    //TODO give exp
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
      double percent = (double) currentBrewProgress / (double) potionBrewDuration;
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
    double numerator = 24 * potionBrewDuration;
    int usedGlassPanes = (int) (currentBrewProgress / (potionBrewDuration != 0 ? potionBrewDuration : 1));
    int glassToPopulate = (int) ((currentBrewProgress / potionBrewDuration) * 24);
    double percent = 100 * ((double) currentBrewProgress / (double) (potionBrewDuration != 0 ? potionBrewDuration : 1));
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
      inv.setItem((i*9) - 1, glass);
    }
    for(HumanEntity viewer : inv.getViewers()){
      ((Player) viewer).updateInventory();
    }
  }

  private boolean canInitBrew(){
    return false;
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
}
