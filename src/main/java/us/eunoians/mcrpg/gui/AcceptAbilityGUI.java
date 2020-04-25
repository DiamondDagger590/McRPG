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

public class AcceptAbilityGUI extends GUI {

  @Getter
  AcceptType acceptType;
  @Getter
  private BaseAbility ability;
  private GUIInventoryFunction buildGUIFunction;


  public AcceptAbilityGUI(McRPGPlayer p, BaseAbility ability, AcceptType acceptType) {
    super(new GUIBuilder(p));
    this.ability = ability;
    this.acceptType = acceptType;
    if(!GUITracker.isPlayerTracked(p)) {
      GUITracker.trackPlayer(p, this);
    }
    if(acceptType == AcceptType.ACCEPT_ABILITY) {
      buildGUIFunction = (GUIBuilder builder) -> {
        FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill().getName()));
		FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ACCEPT_ABILITY_GUI);
        Inventory inv = Bukkit.createInventory(null, guiConfig.getInt("Size"),
                Methods.color(p.getPlayer(), guiConfig.getString("Title.AcceptAbility").replace("%Ability%", ability.getGenericAbility().getName())));
        ArrayList<GUIItem> items = new ArrayList<>();
        String path = ability.getGenericAbility().getName() + "Config.Item.";

        ItemStack confirmItem = new ItemStack(Material.valueOf(guiConfig.getString("AcceptAbility.AcceptItem.Material")), guiConfig.getInt("AcceptAbility.AcceptItem.Amount"));
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(Methods.color(p.getPlayer(), guiConfig.getString("AcceptAbility.AcceptItem.DisplayName")));
        List<String> confirmLore = new ArrayList<>();
        for(String s : Methods.colorLore(config.getStringList(path + "MenuLore"))){
          if(s.contains("%UnlockLevel%")){
            continue;
          }
          confirmLore.add(s);
        }
        confirmMeta.setLore(confirmLore);
        confirmItem.setItemMeta(confirmMeta);
        items.add(new GUIItem(confirmItem, guiConfig.getInt("AcceptAbility.AcceptItem.Slot")));

        ItemStack denyItem = new ItemStack(Material.valueOf(guiConfig.getString("AcceptAbility.DenyItem.Material")), guiConfig.getInt("AcceptAbility.DenyItem.Amount"));
        ItemMeta denyMeta = denyItem.getItemMeta();
        denyMeta.setDisplayName(Methods.color(p.getPlayer(), guiConfig.getString("AcceptAbility.DenyItem.DisplayName")));
        denyMeta.setLore(Methods.colorLore(guiConfig.getStringList("AcceptAbility.DenyItem.Lore")));
        denyItem.setItemMeta(denyMeta);
        items.add(new GUIItem(denyItem, guiConfig.getInt("AcceptAbility.DenyItem.Slot")));

        ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
                config.getInt(path + "Amount"));
        ItemMeta abilityMeta = abilityItem.getItemMeta();
        abilityMeta.setDisplayName(Methods.color(p.getPlayer(), config.getString(path + "DisplayName")));
        List<String> lore = Methods.colorLore(guiConfig.getStringList("AcceptAbility.AbilityItem.Lore"));
        for(String s : config.getStringList(path + "MenuLore")){
          if(s.contains("%UnlockLevel%")){
            continue;
          }
          lore.add(Methods.color(s));
        }
        abilityMeta.setLore(lore);
        abilityItem.setItemMeta(abilityMeta);
        items.add(new GUIItem(abilityItem, guiConfig.getInt("AcceptAbility.AbilityItem.Slot")));

        ItemStack filler = new ItemStack(Material.AIR);
        if(guiConfig.contains("FillerItem")){
          filler = new ItemStack(Material.valueOf(guiConfig.getString("FillerItem.Material")), guiConfig.getInt("FillerItem.Amount"));
          ItemMeta fillerMeta = filler.getItemMeta();
          fillerMeta.setDisplayName(Methods.color(p.getPlayer(), guiConfig.getString("FillerItem.DisplayName")));
          fillerMeta.setLore(Methods.colorLore(guiConfig.getStringList("FillerItem.Material")));
          filler.setItemMeta(fillerMeta);
        }
        return Methods.fillInventory(inv, filler, items);
      };
    }
    else {
      buildGUIFunction = (GUIBuilder builder) -> {
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ACCEPT_ABILITY_GUI);
        FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill().getName()));
        Inventory inv = Bukkit.createInventory(null, guiConfig.getInt("Size"),
          Methods.color(p.getPlayer(), guiConfig.getString("Title.UpgradeAbility").replace("%Ability%", ability.getGenericAbility().getName())
                                         .replace("%Tier%", Methods.convertToNumeral(ability.getCurrentTier() + 1))));
        ArrayList<GUIItem> items = new ArrayList<>();
  
        ItemStack confirmItem = new ItemStack(Material.valueOf(guiConfig.getString("UpgradeAbility.AcceptItem.Material")), guiConfig.getInt("UpgradeAbility.AcceptItem.Amount"));
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(Methods.color(p.getPlayer(), guiConfig.getString("UpgradeAbility.AcceptItem.DisplayName")));
        confirmMeta.setLore(Methods.colorLore(guiConfig.getStringList("UpgradeAbility.AcceptItem.Lore")));
        confirmItem.setItemMeta(confirmMeta);
        items.add(new GUIItem(confirmItem, guiConfig.getInt("UpgradeAbility.AcceptItem.Slot")));
  
        ItemStack denyItem = new ItemStack(Material.valueOf(guiConfig.getString("UpgradeAbility.DenyItem.Material")), guiConfig.getInt("UpgradeAbility.DenyItem.Amount"));
        ItemMeta denyMeta = denyItem.getItemMeta();
        denyMeta.setDisplayName(Methods.color(p.getPlayer(), guiConfig.getString("UpgradeAbility.DenyItem.DisplayName")));
        denyMeta.setLore(Methods.colorLore(guiConfig.getStringList("UpgradeAbility.DenyItem.Lore")));
        denyItem.setItemMeta(denyMeta);
        items.add(new GUIItem(denyItem, guiConfig.getInt("UpgradeAbility.DenyItem.Slot")));
  
        String path = ability.getGenericAbility().getName() + "Config.Item.";
        ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
          config.getInt(path + "Amount"));
        ItemMeta abilityMeta = abilityItem.getItemMeta();
        abilityMeta.setDisplayName(Methods.color(p.getPlayer(), config.getString(path + "DisplayName")));
        abilityMeta.setLore(Methods.colorLore(guiConfig.getStringList("UpgradeAbility.AbilityItem.Lore")));
        abilityItem.setItemMeta(abilityMeta);
        items.add(new GUIItem(abilityItem, guiConfig.getInt("UpgradeAbility.AbilityItem.Slot")));
  
        ItemStack filler = new ItemStack(Material.AIR);
        if(guiConfig.contains("FillerItem")){
          filler = new ItemStack(Material.valueOf(guiConfig.getString("FillerItem.Material")), guiConfig.getInt("FillerItem.Amount"));
          ItemMeta fillerMeta = filler.getItemMeta();
          fillerMeta.setDisplayName(Methods.color(p.getPlayer(), guiConfig.getString("FillerItem.DisplayName")));
          fillerMeta.setLore(Methods.colorLore(guiConfig.getStringList("FillerItem.Material")));
          filler.setItemMeta(fillerMeta);
        }
        return Methods.fillInventory(inv, filler, items);
      };
    }
    this.getGui().setBuildGUIFunction(buildGUIFunction);
    this.getGui().rebuildGUI();
  }

  public enum AcceptType {
    ACCEPT_UPGRADE,
    ACCEPT_ABILITY
  }
}
