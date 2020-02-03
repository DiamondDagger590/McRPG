package us.eunoians.mcrpg.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditLoadoutSelectGUI extends GUI {


  private GUIInventoryFunction buildGUIFunction;
  @Getter
  private ArrayList<BaseAbility> defaultAbilityList = new ArrayList<>();

  public EditLoadoutSelectGUI(McRPGPlayer player) {
    super(new GUIBuilder(player));
    buildGUIFunction = (GUIBuilder builder) -> {
      FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EDIT_LOADOUT_SELECT_GUI);
      String title = Methods.color(player.getPlayer(), guiConfig.getString("Title"));
      Inventory inv = Bukkit.createInventory(null, guiConfig.getInt("Size"),
              title);
      ArrayList<GUIItem> items = new ArrayList<>();

      ItemStack defaultAbilities = new ItemStack(Material.valueOf(guiConfig.getString("DefaultAbilitiesItem.Material")), guiConfig.getInt("DefaultAbilitiesItem.Amount"));
      ItemMeta defaultMeta = defaultAbilities.getItemMeta();
      defaultMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("DefaultAbilitiesItem.DisplayName")));
      defaultMeta.setLore(Methods.colorLore(guiConfig.getStringList("DefaultAbilitiesItem.Lore")));
      defaultAbilities.setItemMeta(defaultMeta);
      items.add(new GUIItem(defaultAbilities, guiConfig.getInt("DefaultAbilitiesItem.Slot")));

      ItemStack replaceAbilities = new ItemStack(Material.valueOf(guiConfig.getString("ReplaceAbilitiesItem.Material")), guiConfig.getInt("ReplaceAbilitiesItem.Amount"));
      ItemMeta replaceMeta = replaceAbilities.getItemMeta();
      replaceMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("ReplaceAbilitiesItem.DisplayName")));
      ArrayList<String> lore = new ArrayList<>();
      if(player.getEndTimeForReplaceCooldown() != 0) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(player.getEndTimeForReplaceCooldown());
        lore = (ArrayList<String>) convertMillis(temp.getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), guiConfig);
      }
      else{
        lore.clear();
      }
      replaceMeta.setLore(lore);

      replaceAbilities.setItemMeta(replaceMeta);
      items.add(new GUIItem(replaceAbilities, guiConfig.getInt("ReplaceAbilitiesItem.Slot")));

      ItemStack unlockedAbilities = new ItemStack(Material.valueOf(guiConfig.getString("UnlockedAbilitiesItem.Material")), guiConfig.getInt("UnlockedAbilitiesItem.Amount"));
      ItemMeta unlockedMeta = unlockedAbilities.getItemMeta();
      unlockedMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("UnlockedAbilitiesItem.DisplayName")));
      unlockedMeta.setLore(Methods.colorLore(guiConfig.getStringList("UnlockedAbilitiesItem.Lore")));
      unlockedAbilities.setItemMeta(unlockedMeta);
      items.add(new GUIItem(unlockedAbilities, guiConfig.getInt("UnlockedAbilitiesItem.Slot")));

      ItemStack back = new ItemStack(Material.valueOf(guiConfig.getString("BackButton.Material")));
      ItemMeta backMeta = back.getItemMeta();
      backMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("BackButton.DisplayName")));
      backMeta.setLore(Methods.colorLore(guiConfig.getStringList("BackButton.Lore")));
      back.setItemMeta(backMeta);
      items.add(new GUIItem(back, guiConfig.getInt("BackButton.Slot")));
  
      ItemStack filler = new ItemStack(Material.AIR);
      if(guiConfig.contains("FillerItem")){
        filler = new ItemStack(Material.valueOf(guiConfig.getString("FillerItem.Material")), guiConfig.getInt("FillerItem.Amount"));
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("FillerItem.DisplayName")));
        fillerMeta.setLore(Methods.colorLore(guiConfig.getStringList("FillerItem.Lore")));
        filler.setItemMeta(fillerMeta);
      }
      return Methods.fillInventory(inv, filler, items);
    };
    this.getGui().setBuildGUIFunction(buildGUIFunction);
    this.getGui().rebuildGUI();
  }

  private static List<String> convertMillis(long milliseconds, FileConfiguration guiConfig) {
    long seconds, minutes, hours;
    seconds = milliseconds / 1000;
    minutes = seconds / 60;
    seconds = seconds % 60;
    hours = minutes / 60;
    minutes = minutes % 60;
    List<String> lore = new ArrayList<>();
    for(String s : guiConfig.getStringList("ReplaceAbilitiesItem.Lore")){
      s = Methods.color(s.replace("%Hours%", Long.toString(hours)).replace("%Minutes%", Long.toString(minutes)).replace("%Seconds%", Long.toString(seconds)));
      lore.add(s);
    }
    return lore;
  }

}
