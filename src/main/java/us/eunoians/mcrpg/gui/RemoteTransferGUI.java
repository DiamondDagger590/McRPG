package us.eunoians.mcrpg.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteTransferGUI extends GUI {

  @Getter
  private RemoteTransfer remoteTransfer;

  private GUIInventoryFunction buildGUIFunction;


  public RemoteTransferGUI(McRPGPlayer p, BaseAbility ability) {
    super(new GUIBuilder(p));
    this.remoteTransfer = (RemoteTransfer) ability;
    if(!GUITracker.isPlayerTracked(p)) {
      GUITracker.trackPlayer(p, this);
    }

    buildGUIFunction = (GUIBuilder builder) -> {
      FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(remoteTransfer.getGenericAbility().getSkill().getName()));
      FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.REMOTE_TRANSFER_GUI);
      Inventory inv = Bukkit.createInventory(null, config.getInt("RemoteTransferConfig.Tier" + Methods.convertToNumeral(remoteTransfer.getCurrentTier()) + ".InvSize"),
              Methods.color(getPlayer().getPlayer(), guiConfig.getString("Title")));
      ArrayList<GUIItem> items = new ArrayList<>();
      HashMap<Material, Boolean> itemsToSync = remoteTransfer.getItemsToSync();
      int counter = 0;
      for(String cat : config.getStringList("RemoteTransferConfig.Tier" + Methods.convertToNumeral(remoteTransfer.getCurrentTier()) + ".Categories")) {
        List<Material> blocksInCat = config.getStringList("RemoteTransferConfig.Categories." + cat).stream().map(Material::getMaterial).collect(Collectors.toList());
        for(Material mat : blocksInCat) {
          if(itemsToSync.containsKey(mat)) {
            ItemStack displayItem = new ItemStack(mat, 1);
            ItemMeta meta = displayItem.getItemMeta();
            meta.setLore(Methods.colorLore(guiConfig.getStringList("ToggleLore")));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            displayItem.setItemMeta(meta);
            if(itemsToSync.get(mat)) {
              displayItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            }
            items.add(new GUIItem(displayItem, counter));
          }
          else {
            itemsToSync.put(mat, true);
            ItemStack displayItem = new ItemStack(mat, 1);
            ItemMeta meta = displayItem.getItemMeta();
            meta.setLore(Methods.colorLore(guiConfig.getStringList("ToggleLore")));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            displayItem.setItemMeta(meta);
            displayItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            items.add(new GUIItem(displayItem, counter));
          }
          counter++;
        }
      }
      p.saveData();


      String path = ability.getGenericAbility().getName() + "Config.Item.";
      ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
              config.getInt(path + "Amount"));
      ItemMeta abilityMeta = abilityItem.getItemMeta();
      abilityMeta.setDisplayName(Methods.color(p.getPlayer(), config.getString(path + "DisplayName")));
      abilityMeta.setLore(Methods.colorLore(guiConfig.getStringList("ToggleAbilityLore")));
      abilityMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      abilityItem.setItemMeta(abilityMeta);
      if(remoteTransfer.isToggled()) {
        abilityItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
      }

      items.add(new GUIItem(abilityItem, inv.getSize() - 1));
  
      ItemStack filler = new ItemStack(Material.AIR);
      if(guiConfig.contains("FillerItem")){
        filler = new ItemStack(Material.valueOf(guiConfig.getString("FillerItem.Material")), guiConfig.getInt("FillerItem.Amount"));
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(Methods.color(p.getPlayer(), guiConfig.getString("FillerItem.DisplayName")));
        fillerMeta.setLore(Methods.colorLore(guiConfig.getStringList("FillerItem.Lore")));
        filler.setItemMeta(fillerMeta);
      }
      return Methods.fillInventory(inv, filler, items);
    };

    this.getGui().setBuildGUIFunction(buildGUIFunction);
    this.getGui().rebuildGUI();
  }
}
