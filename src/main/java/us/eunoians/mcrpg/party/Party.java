package us.eunoians.mcrpg.party;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.PartyPermissions;
import us.eunoians.mcrpg.types.PartyRoles;
import us.eunoians.mcrpg.types.PartyUpgrades;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Party{
  
  @Getter
  private Map<PartyPermissions, PartyRoles> partyPermissions = new HashMap<>();
  @Getter
  private Map<PartyUpgrades, Integer> partyUpgrades = new HashMap<>();
  @Getter
  private Map<UUID, PartyMember> partyMembers = new HashMap<>();
  
  @Getter
  @Setter
  private String name;
  
  @Getter
  private UUID partyID;
  
  @Getter
  @Setter
  private int partyUpgradePoints;
  
  @Getter
  private Inventory partyBank;
  
  @Getter
  private Inventory privateBank;
  
  private FileConfiguration partyFileConfiguration;
  private File partyFile;
  
  Party(UUID partyOwner, String partyName){
    partyID = UUID.randomUUID();
    
    File partyFolder = new File(McRPG.getInstance().getDataFolder(), "parties");
    if(!partyFolder.exists()){
      partyFolder.mkdirs();
    }
    partyFile = new File(McRPG.getInstance().getDataFolder(), "parties" + File.separator + partyID.toString() + ".yml");
    if(!partyFile.exists()){
      try{
        partyFile.createNewFile();
      }catch(IOException e){
        e.printStackTrace();
      }
    }
    partyFileConfiguration = YamlConfiguration.loadConfiguration(partyFile);
    
    PartyMember owner = new PartyMember(partyOwner);
    owner.setPartyRole(PartyRoles.OWNER);
    String ownerKey = "PartyMembers." + partyOwner.toString() + ".";
    partyFileConfiguration.set(ownerKey + "JoinDate", owner.getJoinDate());
    partyFileConfiguration.set(ownerKey + "Role", owner.getPartyRole().getId());
    partyMembers.put(partyOwner, owner);
    
    this.name = partyName;
    this.partyUpgradePoints = 0;
    
    initBanks();
    initPerms();
    initUpgrades();
    saveParty();
  }
  
  Party(File partyFile){
    this.partyID = UUID.fromString(partyFile.getName().replace(".yml", ""));
    this.partyFile = partyFile;
    this.partyFileConfiguration = YamlConfiguration.loadConfiguration(partyFile);
    initBanks();
    for(String uuid : partyFileConfiguration.getConfigurationSection("PartyMembers").getKeys(false)){
      UUID playerUUID = UUID.fromString(uuid);
      PartyRoles partyRole = PartyRoles.getRoleFromId(partyFileConfiguration.getInt("PartyMembers." + uuid + ".Role"));
      PartyMember partyMember = new PartyMember(playerUUID, partyRole, partyFileConfiguration.getLong("PartyMembers." + uuid + ".JoinDate"));
      partyMembers.put(playerUUID, partyMember);
    }
    this.name = partyFileConfiguration.getString("PartyName");
    this.partyUpgradePoints = partyFileConfiguration.getInt("PartyUpgradePoints");
    for(String partyPerm : partyFileConfiguration.getConfigurationSection("Permissions").getKeys(false)){
      PartyPermissions partyPermission = PartyPermissions.getPartyPermission(partyPerm);
      partyPermissions.put(partyPermission, PartyRoles.getRoleFromId(partyFileConfiguration.getInt("Permissions." + partyPerm)));
    }
    for(String upgrade : partyFileConfiguration.getConfigurationSection("Upgrades").getKeys(false)){
      partyUpgrades.put(PartyUpgrades.getPartyUpgrades(upgrade), partyFileConfiguration.getInt("Upgrades." + upgrade));
    }
    for(String s : partyFileConfiguration.getConfigurationSection("PartyBank").getKeys(false)){
      int i = Integer.parseInt(s.replace("Item", ""));
      partyBank.setItem(i, partyFileConfiguration.getItemStack("PartyBank." + s));
    }
    for(String s : partyFileConfiguration.getConfigurationSection("PrivateBank").getKeys(false)){
      int i = Integer.parseInt(s.replace("Item", ""));
      privateBank.setItem(i, partyFileConfiguration.getItemStack("PrivateBank." + s));
    }
  }
  
  private void initPerms(){
    partyPermissions.put(PartyPermissions.UPGRADE_PARTY, PartyRoles.OWNER);
    partyPermissions.put(PartyPermissions.PVP, PartyRoles.MEMBER);
    partyPermissions.put(PartyPermissions.PRIVATE_BANK, PartyRoles.MOD);
    partyPermissions.put(PartyPermissions.KICK_PLAYERS, PartyRoles.MOD);
    partyPermissions.put(PartyPermissions.INVITE_PLAYERS, PartyRoles.MOD);
  }
  
  private void initUpgrades(){
    partyUpgrades.put(PartyUpgrades.MEMBER_COUNT, 0);
    partyUpgrades.put(PartyUpgrades.EXP_SHARE_AMOUNT, 0);
    partyUpgrades.put(PartyUpgrades.EXP_SHARE_RANGE, 0);
  }
  
  private void initBanks(){
    partyBank = Bukkit.createInventory(null, 27, Methods.color("&5Party Bank"));
    privateBank = Bukkit.createInventory(null, 27, Methods.color("&5Private Bank"));
  }
  
  public void addPlayer(UUID uuid){
    PartyMember newMember = new PartyMember(uuid);
    partyMembers.put(uuid, newMember);
  }
  
  public boolean kickPlayer(UUID uuid){
    if(partyMembers.containsKey(uuid)){
      PartyMember member = partyMembers.remove(uuid);
      if(member.getPartyRole() == PartyRoles.OWNER){
        PartyMember oldestMember = null;
        for(PartyMember partyMember : partyMembers.values()){
          if(oldestMember == null){
            oldestMember = partyMember;
            continue;
          }
          if(oldestMember.getPartyRole() == PartyRoles.MOD && partyMember.getPartyRole() == PartyRoles.MEMBER){
            continue;
          }
          if(oldestMember.getPartyRole() == PartyRoles.MEMBER && partyMember.getPartyRole() == PartyRoles.MOD){
            oldestMember = partyMember;
            continue;
          }
          if(oldestMember.getJoinDate() > partyMember.getJoinDate()){
            oldestMember = partyMember;
          }
          else{
            Bukkit.getLogger().log(Level.WARNING, Methods.color("&cThere was an issue with promoting a new player to owner"));
          }
        }
        oldestMember.setPartyRole(PartyRoles.OWNER);
      }
      return true;
    }
    return false;
  }
  
  public boolean invitePlayer(UUID uuid){
    try{
      McRPGPlayer mcRPGPlayer = PlayerManager.getPlayer(uuid);
      if(mcRPGPlayer.getPartyID() == null){
        PartyInvite partyInvite = new PartyInvite(partyID, uuid);
        mcRPGPlayer.getPartyInvites().enqueue(partyInvite);
        return true;
      }
      return false;
    }catch(McRPGPlayerNotFoundException e){
      return false;
    }
  }
  
  public void disband(){
    for(UUID playerUUID : partyMembers.keySet()){
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(playerUUID);
      }catch(McRPGPlayerNotFoundException e){
        mp = new McRPGPlayer(playerUUID);
      }
      mp.setPartyID(null);
      mp.saveData();
    }
  }
  
  public void saveParty(){
    partyFileConfiguration.set("PartyMembers", null);
    String partyMemberKey = "PartyMembers.";
    for(UUID uuid : partyMembers.keySet()){
      PartyMember partyMember = partyMembers.get(uuid);
      String memberKey = partyMemberKey + uuid.toString() + ".";
      partyFileConfiguration.set(memberKey + "JoinDate", partyMember.getJoinDate());
      partyFileConfiguration.set(memberKey + "Role", partyMember.getPartyRole().getId());
    }
    String permKey = "Permissions.";
    for(PartyPermissions partyPermission : partyPermissions.keySet()){
      partyFileConfiguration.set(permKey + partyPermission.getName().replace(" ", ""), partyPermission.getId());
    }
    String upgradeKey = "Upgrades.";
    for(PartyUpgrades partyUpgrade : PartyUpgrades.values()){
      partyFileConfiguration.set(upgradeKey + partyUpgrade.getName().replace(" ", ""), partyUpgrades.get(partyUpgrade));
    }
    partyFileConfiguration.set("PartyName", name);
    partyFileConfiguration.set("PartyUpgradePoints", partyUpgradePoints);
    //Save the public party bank
    for(int i = 0; i < partyBank.getSize(); i++){
      ItemStack item = partyBank.getItem(i);
      if(item != null && item.getType() != Material.AIR){
        partyFileConfiguration.set("PartyBank.Item" + i, item);
      }
    }
    //Save the private bank
    for(int i = 0; i < privateBank.getSize(); i++){
      ItemStack item = privateBank.getItem(i);
      if(item != null && item.getType() != Material.AIR){
        partyFileConfiguration.set("PrivateBank.Item" + i, item);
      }
    }
    try{
      partyFileConfiguration.save(partyFile);
    }catch(IOException e){
      e.printStackTrace();
    }
  }
}
