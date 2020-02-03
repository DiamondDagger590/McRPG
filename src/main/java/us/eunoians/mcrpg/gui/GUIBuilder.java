package us.eunoians.mcrpg.gui;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GUIBuilder{
  
  @Getter
  private String path;
  @Getter
  private Inventory inv;
  @Getter
  private String rawPath;
  @Getter
  private ArrayList<GUIEventBinder> boundEvents;
  @Getter
  private FileConfiguration config;
  @Getter
  @Setter
  private ArrayList<GUIItem> items;
  @Getter
  private McRPGPlayer player;
  
  @Getter
  @Setter
  private GUIPlaceHolderFunction replacePlaceHoldersFunction = (GUIBuilder guiBuilder) -> {
    if(guiBuilder.getRawPath().equalsIgnoreCase("MainGUI")){
      for(int i = 0; i < guiBuilder.getInv().getSize(); i++){
        ItemStack item = guiBuilder.getInv().getItem(i);
        if(item.hasItemMeta() && item.getItemMeta().hasLore()){
          ItemMeta meta = item.getItemMeta();
          List<String> lore = new ArrayList<>();
          meta.getLore().stream().forEach(s -> lore.add(s.replaceAll("%Power_Level%", Integer.toString(guiBuilder.getPlayer().getPowerLevel()))
                                                          .replaceAll("%Ability_Points%", Integer.toString(guiBuilder.getPlayer().getAbilityPoints()))));
          meta.setLore(lore);
          item.setItemMeta(meta);
          guiBuilder.getInv().setItem(i, item);
        }
        continue;
      }
    }
  };
  
  @Getter
  @Setter
  private GUIInventoryFunction buildGUIFunction = (GUIBuilder builder) -> {
    Inventory inv = Bukkit.createInventory(null, config.getInt(path + "Size"),
      Methods.color(player.getPlayer(), config.getString(path + "Title")));
    items = new ArrayList<>();
    for(String itemName : config.getConfigurationSection(path + "Items").getKeys(false)){
      ItemStack item;
      Material type = Material.getMaterial(config.getString(path + "Items." + itemName + ".Material"));
      item = new ItemStack(type, 1);
      ItemMeta meta = item.getItemMeta();
      if(type.equals(Material.PLAYER_HEAD)){
        SkullMeta sm = (SkullMeta) meta;
        sm.setOwningPlayer(player.getOfflineMcMMOPlayer());
      }
      meta.setDisplayName(Methods.color(player.getPlayer(), config.getString(path + "Items." + itemName + ".Name")));
      if(config.contains(path + "Items." + itemName + ".Lore")){
        List<String> lore = config.getStringList(path + "Items." + itemName + ".Lore");
        lore = Methods.colorLore(lore);
        meta.setLore(lore);
      }
      item.setItemMeta(meta);
      item.getItemMeta().addItemFlags(ItemFlag.HIDE_ENCHANTS);
      GUIItem i = new GUIItem(item, config.getInt(path + ".Items." + itemName + ".Slot"));
      items.add(i);
    }
    ItemStack filler;
    if(!config.contains(path + "FillerItem")){
      return inv;
    }
    Material fillerType = Material.getMaterial(config.getString(path + "FillerItem.Material"));
    filler = new ItemStack(fillerType, 1);
    ItemMeta meta = filler.getItemMeta();
    meta.setDisplayName(Methods.color(player.getPlayer(), config.getString(path + "FillerItem.Name")));
    filler.setItemMeta(meta);
    if(config.contains(path + "FillerItem.Lore")){
      List<String> lore = config.getStringList(path + "FillerItem.Lore");
      lore = Methods.colorLore(lore);
      meta.setLore(lore);
      filler.setItemMeta(meta);
    }
    return Methods.fillInventory(inv, filler, items);
  };
  
  @Getter
  @Setter
  private GUIBindEventFunction bindEventsFunction = (GUIBuilder builder) -> {
    ArrayList<GUIEventBinder> binder = new ArrayList<>();
    if(!config.contains(path + "Events")){
      return binder;
    }
    for(String slotString : config.getConfigurationSection(path + "Events").getKeys(false)){
      GUIEventBinder boundEvent = new GUIEventBinder(Integer.parseInt(slotString), config.getStringList(path + "Events." + slotString));
      binder.add(boundEvent);
    }
    return binder;
  };
  
  /**
   * Used when loading a gui from a file. Typical usage would be when loading a custom gui that isnt defined in FileManager
   *
   * @param fileName
   * @param guiPath
   * @param player
   */
  public GUIBuilder(String fileName, String guiPath, McRPGPlayer player){
    this.player = player;
    File f = new File(McRPG.getInstance().getDataFolder(), File.separator + "guis" + File.separator + fileName);
    this.config = YamlConfiguration.loadConfiguration(f);
    this.rawPath = guiPath;
    this.path = "Gui." + guiPath + ".";
    this.inv = buildGUIFunction.generateInventory(this);
    this.boundEvents = bindEventsFunction.bindEvents(this);
  }
  
  /**
   * Used when loading a gui from a file thats been preloaded
   *
   * @param guiPath
   * @param config
   * @param player
   */
  public GUIBuilder(String guiPath, FileConfiguration config, McRPGPlayer player){
    this.player = player;
    this.rawPath = guiPath;
    this.config = config;
    this.path = "Gui." + guiPath + ".";
    this.inv = buildGUIFunction.generateInventory(this);
    this.boundEvents = bindEventsFunction.bindEvents(this);
  }
  
  public GUIBuilder(McRPGPlayer player){
    this.player = player;
  }
  
  public GUIBuilder(){
  }
  
  ;
  
  /**
   * Construct a new gui from the provided elements. Reuse the FileConfiguration so that way it doesnt have to do an I/O
   *
   * @return A new gui builder that is the same contents as the previous one but this will update placeholders.
   */
  public GUIBuilder clone(){
    return new GUIBuilder(this.rawPath, this.config, this.player);
  }
  
  public void setNewInventory(Inventory newInv){
    this.inv = newInv;
  }
  
  public void rebuildGUI(){
    this.inv = buildGUIFunction.generateInventory(this);
  }
  
  public void rebindEvents(){
    this.boundEvents = bindEventsFunction.bindEvents(this);
  }
  
  public void replacePlaceHolders(){
    replacePlaceHoldersFunction.replacePlaceHolders(this);
  }
  
}
