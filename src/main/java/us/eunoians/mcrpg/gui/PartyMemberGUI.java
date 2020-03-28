package us.eunoians.mcrpg.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.PartyNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.party.PartyManager;
import us.eunoians.mcrpg.party.PartyMember;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.util.SkullCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PartyMemberGUI extends GUI{
  
  private GUIInventoryFunction guiInventoryFunction;
  
  private GUIPlaceHolderFunction guiPlaceHolderFunction;
  
  /**
   * This should only be called when the player is actually in a party.
   * @param mcRPGPlayer The player that this party is being opened for
   * @throws us.eunoians.mcrpg.api.exceptions.PartyNotFoundException if the player does not have a party
   */
  public PartyMemberGUI(McRPGPlayer mcRPGPlayer) throws PartyNotFoundException{
    super(new GUIBuilder(mcRPGPlayer));
    PartyManager partyManager = McRPG.getInstance().getPartyManager();
    if(mcRPGPlayer.getPartyID() == null || partyManager.getParty(mcRPGPlayer.getPartyID()) == null){
      throw new PartyNotFoundException("The player is either no in a party or the party is invalid and as such could not be handled");
    }
    Party party = partyManager.getParty(mcRPGPlayer.getPartyID());
    guiInventoryFunction = (GUIBuilder builder) -> {
      FileConfiguration memberFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_MEMBER_GUI);
      Map<UUID, PartyMember> partyMemberMap = party.getPartyMembers();
      int size = (Math.min(54, partyMemberMap.size() + (partyMemberMap.size() % 9 != 0 ? ((9 - (partyMemberMap.size() % 9))) : 0)));
      Inventory inventory = Bukkit.createInventory(null, size, Methods.color(memberFile.getString("Title")));
      int i = 0;
      for(UUID uuid : partyMemberMap.keySet()){
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        ItemStack item = new ItemStack(Material.matchMaterial(memberFile.getString("PartyMemberItem.Material", "PLAYER_HEAD")));
        ItemMeta meta = item.getItemMeta();
        if(item.getType().equals(Material.PLAYER_HEAD)){
          if(SkullCache.headMap.containsKey(uuid)){
            item = SkullCache.headMap.get(uuid).clone();
            meta = item.getItemMeta();
          }
          else{
            SkullMeta sm = (SkullMeta) meta;
            if(memberFile.contains("PartyMemberItem.Owner")){
              if(memberFile.getString("PartyMemberItem.Owner").equalsIgnoreCase("%Player%")){
                sm.setOwningPlayer(offlinePlayer);
              }
              else{
                sm.setOwningPlayer(Bukkit.getOfflinePlayer(memberFile.getString("PartyMemberItem.Owner")));
              }
            }
            else{
              sm.setOwningPlayer(offlinePlayer);
            }
            item.setItemMeta(sm);
            SkullCache.headMap.put(uuid, item);
            item = item.clone();
            meta = item.getItemMeta();
          }
        }
        List<String> lore = new ArrayList<>();
        for(String s : memberFile.getStringList("PartyMemberItem.Lore")){
          lore.add(s.replace("%Last_Login%", offlinePlayer.isOnline() ? memberFile.getString("Strings.CurrentlyOnline", "Online")
                                               : memberFile.getString("Strings.Offline").replace("%Hour_Amount%", Integer.toString(Methods.findHoursDiffFromCurrent(offlinePlayer.getLastPlayed()))))
          .replace("%Party_Role%", partyMemberMap.get(uuid).getPartyRole().getName()));
        }
        meta.setLore(Methods.colorLore(lore));
        meta.setDisplayName(Methods.color(memberFile.getString("PartyMemberItem.DisplayName", "&5" + offlinePlayer.getName()).replace("%Player%", offlinePlayer.getName())));
        item.setItemMeta(meta);
        inventory.setItem(i, item);
        i++;
      }
      return inventory;
    };
    this.getGui().setBuildGUIFunction(guiInventoryFunction);
    this.getGui().rebuildGUI();
  }
}
