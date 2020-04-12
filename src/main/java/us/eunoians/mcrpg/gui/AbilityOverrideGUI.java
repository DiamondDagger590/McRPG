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
import java.util.List;

public class AbilityOverrideGUI extends GUI{
  
  @Getter
  private BaseAbility abiltyToReplace;
  @Getter
  private BaseAbility replaceAbility;
  
  private GUIInventoryFunction buildGUIFunction;
  
  public AbilityOverrideGUI(McRPGPlayer player, BaseAbility abiltyToReplace, BaseAbility replaceAbility){
    super(new GUIBuilder(player));
    this.abiltyToReplace = abiltyToReplace;
    this.replaceAbility = replaceAbility;
    buildGUIFunction = (GUIBuilder builder) -> {
      FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(abiltyToReplace.getGenericAbility().getSkill().getName()));
      FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ABILITY_OVERRIDE_GUI);
      Inventory inv = Bukkit.createInventory(null, guiConfig.getInt("Size"),
        Methods.color(player.getPlayer(), guiConfig.getString("Title").replace("%OldAbility%", abiltyToReplace.getGenericAbility().getName())
                                            .replace("%NewAbility%", replaceAbility.getGenericAbility().getName())));
      ArrayList<GUIItem> items = new ArrayList<>();
      
      ItemStack confirmItem = new ItemStack(Material.valueOf(guiConfig.getString("ConfirmItem.Material")), guiConfig.getInt("ConfirmItem.Amount"));
      ItemMeta confirmMeta = confirmItem.getItemMeta();
      confirmMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("ConfirmItem.DisplayName")));
      confirmMeta.setLore(Methods.colorLore(guiConfig.getStringList("ConfirmItem.Lore")));
      confirmItem.setItemMeta(confirmMeta);
      items.add(new GUIItem(confirmItem, guiConfig.getInt("ConfirmItem.Slot")));
      
      ItemStack denyItem = new ItemStack(Material.valueOf(guiConfig.getString("DenyItem.Material")), guiConfig.getInt("DenyItem.Amount"));
      ItemMeta denyMeta = denyItem.getItemMeta();
      denyMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("DenyItem.DisplayName")));
      denyMeta.setLore(Methods.colorLore(guiConfig.getStringList("DenyItem.Lore")));
      denyItem.setItemMeta(denyMeta);
      items.add(new GUIItem(denyItem, guiConfig.getInt("DenyItem.Slot")));
      
      String path = abiltyToReplace.getGenericAbility().getName() + "Config.Item.";
      ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
        config.getInt(path + "Amount"));
      ItemMeta abilityMeta = abilityItem.getItemMeta();
      abilityMeta.setDisplayName(Methods.color(player.getPlayer(), config.getString(path + "DisplayName")));
      List<String> lore = new ArrayList<>();
      for(String s : config.getStringList(path + "MenuLore")){
        if(s.contains("%UnlockLevel%")){
          continue;
        }
        else{
          lore.add(s);
        }
      }
      for(String s : guiConfig.getStringList("AbilityToBeReplacedItem.Lore")){
        lore.add(0, s);
      }
      abilityMeta.setLore(Methods.colorLore(lore));
      abilityItem.setItemMeta(abilityMeta);
      items.add(new GUIItem(abilityItem, guiConfig.getInt("AbilityToBeReplacedItem.Slot")));
      
      String path1 = replaceAbility.getGenericAbility().getName() + "Config.Item.";
      ItemStack abilityItem1 = new ItemStack(Material.getMaterial(config.getString(path1 + "Material")),
        config.getInt(path1 + "Amount"));
      ItemMeta abilityMeta1 = abilityItem1.getItemMeta();
      abilityMeta1.setDisplayName(Methods.color(player.getPlayer(), config.getString(path1 + "DisplayName")));
      List<String> lore1 = new ArrayList<>();
      for(String s : config.getStringList(path1 + "MenuLore")){
        if(s.contains("%UnlockLevel%")){
          continue;
        }
        else{
          lore1.add(s);
        }
      }
      for(String s : guiConfig.getStringList("AbilityReplacingItem.Lore")){
        lore1.add(0, s);
      }
      abilityMeta1.setLore(Methods.colorLore(lore1));
      abilityItem1.setItemMeta(abilityMeta1);
      items.add(new GUIItem(abilityItem1, guiConfig.getInt("AbilityReplacingItem.Slot")));
      
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
}
