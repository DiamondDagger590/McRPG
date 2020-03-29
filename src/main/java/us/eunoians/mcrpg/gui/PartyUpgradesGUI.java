package us.eunoians.mcrpg.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.PartyUpgrades;
import us.eunoians.mcrpg.util.SkullCache;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyUpgradesGUI extends GUI{
  
  @Getter
  private Party party;
  
  @Getter
  private Map<Integer, PartyUpgrades> partyUpgradesMap = new HashMap<>();
  
  private GUIInventoryFunction buildFunction;
  
  public PartyUpgradesGUI(McRPGPlayer mcRPGPlayer) throws PartyNotFoundException{
    super(new GUIBuilder(mcRPGPlayer));
    
    PartyManager partyManager = McRPG.getInstance().getPartyManager();
    if(mcRPGPlayer.getPartyID() == null || partyManager.getParty(mcRPGPlayer.getPartyID()) == null){
      throw new PartyNotFoundException("The player is either no in a party or the party is invalid and as such could not be handled");
    }
    party = partyManager.getParty(mcRPGPlayer.getPartyID());
    
    FileConfiguration upgradesFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_UPGRADES_GUI);
    
    buildFunction = (GUIBuilder guiBuilder) -> {
      Inventory inventory = Bukkit.createInventory(null, upgradesFile.getInt("Size", 27),
        Methods.color(upgradesFile.getString("Title").replace("%Upgrade_Points%", Integer.toString(party.getPartyUpgradePoints()))));
      List<GUIItem> guiItems = new ArrayList<>();
      if(upgradesFile.contains("MemberCount")){
        ItemStack memberItem = new ItemStack(Material.getMaterial(upgradesFile.getString("MemberCount.Material")));
        ItemMeta itemMeta = memberItem.getItemMeta();
        if(memberItem.getType() == Material.PLAYER_HEAD){
          if(SkullCache.headMap.containsKey(mcRPGPlayer.getUuid())){
            memberItem = SkullCache.headMap.get(mcRPGPlayer.getUuid());
            itemMeta = memberItem.getItemMeta();
          }
          else{
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            if(upgradesFile.contains("MemberCount.Owner")){
              if(upgradesFile.getString("MemberCount.Owner").equalsIgnoreCase("%Player%")){
                skullMeta.setOwningPlayer(mcRPGPlayer.getOfflineMcRPGPlayer());
              }
              else{
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(upgradesFile.getString("MemberCount.Owner")));
              }
            }
            else{
              skullMeta.setOwningPlayer(mcRPGPlayer.getOfflineMcRPGPlayer());
            }
            memberItem.setItemMeta(skullMeta);
            SkullCache.headMap.put(mcRPGPlayer.getUuid(), memberItem);
            memberItem = memberItem.clone();
            itemMeta = memberItem.getItemMeta();
          }
        }
        itemMeta.setDisplayName(Methods.color(upgradesFile.getString("MemberCount.DisplayName")));
        List<String> lore = new ArrayList<>();
        int currentLevel = party.getUpgradeTier(PartyUpgrades.MEMBER_COUNT);
        int maxLevel = PartyUpgrades.getMaxMemberUpgradeTier();
        int memberCount = PartyUpgrades.getMemberCountAtTier(currentLevel);
        for(String s : upgradesFile.getStringList("MemberCount.Lore")){
          lore.add(Methods.color(s.replace("%Current_Level%", Integer.toString(currentLevel))
                                   .replace("%Max_Level%", Integer.toString(maxLevel))
                                   .replace("%Member_Limit%", Integer.toString(memberCount))));
        }
        itemMeta.setLore(lore);
        memberItem.setItemMeta(itemMeta);
        int slot = upgradesFile.getInt("MemberCount.Slot");
        guiItems.add(new GUIItem(memberItem, slot));
        partyUpgradesMap.put(slot, PartyUpgrades.MEMBER_COUNT);
      }
      NumberFormat numberFormat = NumberFormat.getNumberInstance();
      numberFormat.setMinimumFractionDigits(2);
      numberFormat.setMaximumFractionDigits(2);
      if(upgradesFile.contains("ExpShareAmount")){
        ItemStack expShareItem = new ItemStack(Material.getMaterial(upgradesFile.getString("ExpShareAmount.Material")));
        ItemMeta itemMeta = expShareItem.getItemMeta();
        itemMeta.setDisplayName(Methods.color(upgradesFile.getString("ExpShareAmount.DisplayName")));
        List<String> lore = new ArrayList<>();
        int currentLevel = party.getUpgradeTier(PartyUpgrades.EXP_SHARE_AMOUNT);
        int maxLevel = PartyUpgrades.getMaxExpShareTier();
        double shareAmount = PartyUpgrades.getExpShareAmountAtTier(currentLevel);
        for(String s : upgradesFile.getStringList("ExpShareAmount.Lore")){
          lore.add(Methods.color(s.replace("%Current_Level%", Integer.toString(currentLevel)).replace("%Max_Level%", Integer.toString(maxLevel))
          .replace("%Exp_Share_Amount%", numberFormat.format(shareAmount))));
        }
        itemMeta.setLore(lore);
        expShareItem.setItemMeta(itemMeta);
        int slot = upgradesFile.getInt("ExpShareAmount.Slot");
        guiItems.add(new GUIItem(expShareItem, slot));
        partyUpgradesMap.put(slot, PartyUpgrades.EXP_SHARE_AMOUNT);
      }
      if(upgradesFile.contains("ExpShareRange")){
        ItemStack expShareItem = new ItemStack(Material.getMaterial(upgradesFile.getString("ExpShareRange.Material")));
        ItemMeta itemMeta = expShareItem.getItemMeta();
        itemMeta.setDisplayName(Methods.color(upgradesFile.getString("ExpShareRange.DisplayName")));
        List<String> lore = new ArrayList<>();
        int currentLevel = party.getUpgradeTier(PartyUpgrades.EXP_SHARE_RANGE);
        int maxLevel = PartyUpgrades.getMaxExpShareTier();
        double shareRange = PartyUpgrades.getExpShareRangeAtTier(currentLevel);
        for(String s : upgradesFile.getStringList("ExpShareRange.Lore")){
          lore.add(Methods.color(s.replace("%Current_Level%", Integer.toString(currentLevel)).replace("%Max_Level%", Integer.toString(maxLevel))
                     .replace("%Exp_Share_Range%", numberFormat.format(shareRange))));
        }
        itemMeta.setLore(lore);
        expShareItem.setItemMeta(itemMeta);
        int slot = upgradesFile.getInt("ExpShareRange.Slot");
        guiItems.add(new GUIItem(expShareItem, slot));
        partyUpgradesMap.put(slot, PartyUpgrades.EXP_SHARE_RANGE);
      }
      if(upgradesFile.contains("PrivateBankSize")){
        ItemStack bankItem = new ItemStack(Material.getMaterial(upgradesFile.getString("PrivateBankSize.Material")));
        ItemMeta itemMeta = bankItem.getItemMeta();
        itemMeta.setDisplayName(Methods.color(upgradesFile.getString("PrivateBankSize.DisplayName")));
        List<String> lore = new ArrayList<>();
        int currentLevel = party.getUpgradeTier(PartyUpgrades.PRIVATE_BANK_SIZE);
        int maxLevel = PartyUpgrades.getMaxExpShareTier();
        int slots = PartyUpgrades.getPrivateBankSizeAtTier(currentLevel);
        for(String s : upgradesFile.getStringList("PrivateBankSize.Lore")){
          lore.add(Methods.color(s.replace("%Current_Level%", Integer.toString(currentLevel)).replace("%Max_Level%", Integer.toString(maxLevel))
                     .replace("%Private_Bank_Size%", Integer.toString(slots))));
        }
        itemMeta.setLore(lore);
        bankItem.setItemMeta(itemMeta);
        int slot = upgradesFile.getInt("PrivateBankSize.Slot");
        guiItems.add(new GUIItem(bankItem, slot));
        partyUpgradesMap.put(slot, PartyUpgrades.PRIVATE_BANK_SIZE);
      }
      if(upgradesFile.contains("BackButton")){
        ItemStack back = new ItemStack(Material.getMaterial(upgradesFile.getString("BackButton.Material")));
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(Methods.color(upgradesFile.getString("BackButton.DisplayName")));
        backMeta.setLore(Methods.colorLore(upgradesFile.getStringList("BackButton.Lore")));
        back.setItemMeta(backMeta);
        guiItems.add(new GUIItem(back, upgradesFile.getInt("BackButton.Slot")));
      }
      ItemStack filler = new ItemStack(Material.AIR);
      if(upgradesFile.contains("FillerItem")){
        Material fillerType = Material.getMaterial(upgradesFile.getString("FillerItem.Material"));
        filler = new ItemStack(fillerType, 1);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(Methods.color(upgradesFile.getString("FillerItem.DisplayName", "")));
        filler.setItemMeta(meta);
        if(upgradesFile.contains("FillerItem.Lore")){
          meta.setLore(Methods.colorLore(upgradesFile.getStringList("FillerItem.Lore")));
          filler.setItemMeta(meta);
        }
      }
      return Methods.fillInventory(inventory, filler, guiItems);
    };
    
    this.getGui().setBuildGUIFunction(buildFunction);
    this.getGui().rebuildGUI();
  }
}
