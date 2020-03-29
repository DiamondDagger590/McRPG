package us.eunoians.mcrpg.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.PartyPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyRoleGUI extends GUI{
  
  @Getter
  private Party party;
  
  @Getter
  private Map<Integer, PartyPermissions> partyPermissionsMap = new HashMap<>();
  
  private GUIInventoryFunction buildFunction;
  
  public PartyRoleGUI(McRPGPlayer mcRPGPlayer, Party party){
    super(new GUIBuilder(mcRPGPlayer));
    this.party = party;
    
    buildFunction = (GUIBuilder guiBuilder) -> {
      FileConfiguration partyRoleFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_ROLE_GUI);
      Inventory inventory = Bukkit.createInventory(null, partyRoleFile.getInt("Size", 27), Methods.color(partyRoleFile.getString("Title")));
      List<GUIItem> guiItems = new ArrayList<>();
      for(PartyPermissions partyPermission : PartyPermissions.values()){
        String key = partyPermission.getName().replace(" ", "") + ".";
        int slot = partyRoleFile.getInt(key + "Slot");
        key +=  party.getRoleForPermission(partyPermission).getName() + ".";
        ItemStack itemStack = new ItemStack(Material.getMaterial(partyRoleFile.getString(key + "Material")));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Methods.color(partyRoleFile.getString(key + "DisplayName")));
        itemMeta.setLore(Methods.colorLore(partyRoleFile.getStringList(key + "Lore")));
        itemStack.setItemMeta(itemMeta);
        guiItems.add(new GUIItem(itemStack, slot));
        partyPermissionsMap.put(slot, partyPermission);
      }
      ItemStack filler = new ItemStack(Material.AIR);
      if(partyRoleFile.contains("FillerItem")){
        Material fillerType = Material.getMaterial(partyRoleFile.getString("FillerItem.Material"));
        filler = new ItemStack(fillerType, 1);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(Methods.color(partyRoleFile.getString("FillerItem.DisplayName", "")));
        filler.setItemMeta(meta);
        if(partyRoleFile.contains("FillerItem.Lore")){
          meta.setLore(Methods.colorLore(partyRoleFile.getStringList("FillerItem.Lore")));
          filler.setItemMeta(meta);
        }
      }
      return Methods.fillInventory(inventory, filler, guiItems);
    };
    
    this.getGui().setBuildGUIFunction(buildFunction);
    this.getGui().rebuildGUI();
  }
}
