package us.eunoians.mcrpg.party;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcrpg.party.PartyDisbandEvent;
import us.eunoians.mcrpg.api.events.mcrpg.party.PartyExpGainEvent;
import us.eunoians.mcrpg.api.events.mcrpg.party.PartyLevelUpEvent;
import us.eunoians.mcrpg.api.events.mcrpg.party.PlayerJoinPartyEvent;
import us.eunoians.mcrpg.api.events.mcrpg.party.PlayerKickPartyEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.PartyPermissions;
import us.eunoians.mcrpg.types.PartyRoles;
import us.eunoians.mcrpg.types.PartyUpgrades;
import us.eunoians.mcrpg.util.Parser;
import us.eunoians.mcrpg.util.SkullCache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class Party{
  
  private Map<PartyPermissions, PartyRoles> partyPermissions = new HashMap<>();
  private Map<PartyUpgrades, Integer> partyUpgrades = new HashMap<>();
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
  
  @Getter
  @Setter
  private int partyLevel;
  
  @Getter
  @Setter
  private int partyExp;
  
  @Getter
  @Setter
  private int expToLevel;
  
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
    this.partyExp = 0;
    this.partyLevel = 0;
    
    initPerms();
    initUpgrades();
    initBanks();
    calculateExpToLevel();
    saveParty();
  }
  
  Party(File partyFile){
    this.partyID = UUID.fromString(partyFile.getName().replace(".yml", ""));
    this.partyFile = partyFile;
    this.partyFileConfiguration = YamlConfiguration.loadConfiguration(partyFile);
    for(String uuid : partyFileConfiguration.getConfigurationSection("PartyMembers").getKeys(false)){
      UUID playerUUID = UUID.fromString(uuid);
      PartyRoles partyRole = PartyRoles.getRoleFromId(partyFileConfiguration.getInt("PartyMembers." + uuid + ".Role"));
      PartyMember partyMember = new PartyMember(playerUUID, partyRole, partyFileConfiguration.getLong("PartyMembers." + uuid + ".JoinDate"));
      partyMembers.put(playerUUID, partyMember);
    }
    this.name = partyFileConfiguration.getString("PartyName");
    this.partyUpgradePoints = partyFileConfiguration.getInt("PartyUpgradePoints");
    this.partyExp = partyFileConfiguration.getInt("PartyExp");
    this.partyLevel = partyFileConfiguration.getInt("PartyLevel");
    calculateExpToLevel();
    for(String partyPerm : partyFileConfiguration.getConfigurationSection("Permissions").getKeys(false)){
      PartyPermissions partyPermission = PartyPermissions.getPartyPermission(partyPerm);
      partyPermissions.put(partyPermission, PartyRoles.getRoleFromId(partyFileConfiguration.getInt("Permissions." + partyPerm)));
    }
    for(String upgrade : partyFileConfiguration.getConfigurationSection("Upgrades").getKeys(false)){
      partyUpgrades.put(PartyUpgrades.getPartyUpgrades(upgrade), partyFileConfiguration.getInt("Upgrades." + upgrade));
    }
    initBanks();
    if(partyFileConfiguration.contains("PartyBank")){
      for(String s : partyFileConfiguration.getConfigurationSection("PartyBank").getKeys(false)){
        int i = Integer.parseInt(s.replace("Item", ""));
        partyBank.setItem(i, partyFileConfiguration.getItemStack("PartyBank." + s));
      }
    }
    if(partyFileConfiguration.contains("PrivateBank")){
      for(String s : partyFileConfiguration.getConfigurationSection("PrivateBank").getKeys(false)){
        int i = Integer.parseInt(s.replace("Item", ""));
        privateBank.setItem(i, partyFileConfiguration.getItemStack("PrivateBank." + s));
      }
    }
    
    //Preload heads
    for(UUID uuid : getAllMemberUUIDs()){
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
      ItemStack item = new ItemStack(Material.PLAYER_HEAD);
      ItemMeta meta = item.getItemMeta();
      if(SkullCache.headMap.containsKey(uuid)){
        continue;
      }
      else{
        SkullMeta sm = (SkullMeta) meta;
        sm.setOwningPlayer(offlinePlayer);
        item.setItemMeta(sm);
        SkullCache.headMap.put(uuid, item);
      }
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
    partyUpgrades.put(PartyUpgrades.PRIVATE_BANK_SIZE, 0);
  }
  
  private void initBanks(){
    FileConfiguration partyConfiguration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG);
    partyBank = Bukkit.createInventory(null, partyConfiguration.getInt("PartyBank.Size"), Methods.color(partyConfiguration.getString("PartyBank.Title")));
    int size = PartyUpgrades.getPrivateBankSizeAtTier(partyUpgrades.get(PartyUpgrades.PRIVATE_BANK_SIZE));
    size += size % 9 != 0 ? (9 - (size % 9)) : 0;
    privateBank = Bukkit.createInventory(null, size, Methods.color("&5Private Bank"));
  }
  
  /**
   * This will add a player internally to the party.
   * When calling this method, you must set the McRPGPlayer's partyID to the parties UUID manually
   *
   * @param uuid The uuid of the player to add to the party
   * @see McRPGPlayer#setPartyID(UUID)
   * @see #getPartyID()
   */
  public void addPlayer(UUID uuid){
    PlayerJoinPartyEvent playerJoinPartyEvent = new PlayerJoinPartyEvent(uuid, this);
    Bukkit.getPluginManager().callEvent(playerJoinPartyEvent);
    PartyMember newMember = new PartyMember(uuid);
    partyMembers.put(uuid, newMember);
  }
  
  /**
   * This will internally remove a player from the party
   * This method will attempt to promote the oldest party member of the highest rank to owner if the owner somehow is kicked
   * If there is only one person in the party pre kick, then this method will also disband the party
   * When calling this method, you must set the McRPGPlayer's partyID to null manually
   *
   * @param uuid The uuid of the player to kick from the party
   * @return true if the player was successfully kicked.
   * @see McRPGPlayer#setPartyID(UUID)
   */
  public boolean kickPlayer(UUID uuid){
    if(partyMembers.containsKey(uuid)){
      PlayerKickPartyEvent playerKickPartyEvent = new PlayerKickPartyEvent(uuid, this);
      Bukkit.getPluginManager().callEvent(playerKickPartyEvent);
      if(playerKickPartyEvent.isCancelled()){
        return false;
      }
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
        //If it is null then we can assume there are no players left in the party
        if(oldestMember == null){
          McRPG.getInstance().getPartyManager().removeParty(this.partyID);
          return true;
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
  
  public void disband(boolean deleteData){
    PartyDisbandEvent partyDisbandEvent = new PartyDisbandEvent(this);
    Bukkit.getPluginManager().callEvent(partyDisbandEvent);
    for(UUID playerUUID : partyMembers.keySet()){
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(playerUUID);
        mp.emptyTeleportRequests();
      }catch(McRPGPlayerNotFoundException e){
        mp = new McRPGPlayer(playerUUID);
      }
      mp.setPartyID(null);
      mp.saveData();
    }
    if(deleteData){
      partyFile.delete();
    }
  }
  
  public void giveExp(int exp){
    if(exp <= 0){
      return;
    }
    if(partyLevel >= McRPG.getInstance().getPartyManager().getMaxLevel()){
      return;
    }
    PartyExpGainEvent partyExpGainEvent = new PartyExpGainEvent(this, exp);
    Bukkit.getPluginManager().callEvent(partyExpGainEvent);
    if(!partyExpGainEvent.isCancelled()){
      partyExp += partyExpGainEvent.getExpGained();
      int levelsGained = 0;
      while(partyExp >= expToLevel){
        partyLevel++;
        levelsGained++;
        partyExp -= expToLevel;
        calculateExpToLevel();
      }
      if(levelsGained > 0){
        PartyLevelUpEvent partyLevelUpEvent = new PartyLevelUpEvent(this, partyLevel - levelsGained, partyLevel);
        Bukkit.getPluginManager().callEvent(partyLevelUpEvent);
        partyUpgradePoints += Math.max(0, partyLevelUpEvent.getNewLevel() - partyLevelUpEvent.getPreviousLevel());
        partyLevel = partyLevelUpEvent.getNewLevel();
      }
    }
  }
  
  private void calculateExpToLevel(){
    Parser parser = McRPG.getInstance().getPartyManager().getExpEquation();
    parser.setVariable("party_level", partyLevel);
    expToLevel = (int) parser.getValue();
  }
  
  public List<Player> getOnlinePlayers(){
    List<Player> onlinePlayers = new ArrayList<>();
    for(UUID uuid : partyMembers.keySet()){
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
      if(offlinePlayer.isOnline()){
        onlinePlayers.add((Player) offlinePlayer);
      }
    }
    return onlinePlayers;
  }
  
  public int purgeInactive(int hoursLimit){
    Set<UUID> toKick = new HashSet<>();
    for(UUID uuid : partyMembers.keySet()){
      if(Methods.findHoursDiffFromCurrent(Bukkit.getOfflinePlayer(uuid).getLastPlayed()) >= hoursLimit){
        toKick.add(uuid);
      }
    }
    toKick.forEach(this::kickPlayer);
    if(toKick.size() > 0){
      for(UUID uuid : partyMembers.keySet()){
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if(offlinePlayer.isOnline()){
          ((Player) offlinePlayer).sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + "&c" + toKick.size() + " players were removed from your party due to inactivity"));
        }
      }
    }
    saveParty();
    return toKick.size();
  }
  
  public PartyRoles getRoleForPermission(PartyPermissions partyPermission){
    return partyPermissions.get(partyPermission);
  }
  
  public void setRoleForPermission(PartyPermissions permission, PartyRoles role){
    partyPermissions.replace(permission, role);
  }
  
  public int getUpgradeTier(PartyUpgrades partyUpgrade){
    return partyUpgrades.get(partyUpgrade);
  }
  
  public void setUpgradeTier(PartyUpgrades partyUpgrade, int newTier){
    partyUpgrades.replace(partyUpgrade, newTier);
    if(partyUpgrade == PartyUpgrades.PRIVATE_BANK_SIZE){
      int size = PartyUpgrades.getPrivateBankSizeAtTier(partyUpgrades.get(PartyUpgrades.PRIVATE_BANK_SIZE));
      size += size % 9 != 0 ? (9 - (size % 9)) : 0;
      Inventory newInventory = Bukkit.createInventory(null, size, Methods.color("&5Private Bank"));
      newInventory.setContents(privateBank.getContents());
      List<HumanEntity> viewers = privateBank.getViewers();
      privateBank = newInventory;
      for(HumanEntity viewer : viewers){
        viewer.closeInventory();
        new BukkitRunnable(){
          @Override
          public void run(){
            viewer.openInventory(privateBank);
          }
        }.runTaskLater(McRPG.getInstance(), 1);
      }
    }
  }
  
  public PartyMember getPartyMember(UUID uuid){
    return partyMembers.get(uuid);
  }
  
  public boolean isPlayerInParty(UUID uuid){
    return partyMembers.containsKey(uuid);
  }
  
  public Set<UUID> getAllMemberUUIDs(){
    return partyMembers.keySet();
  }
  
  public Collection<PartyMember> getAllMembers(){
    return partyMembers.values();
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
      partyFileConfiguration.set(permKey + partyPermission.getName().replace(" ", ""), partyPermissions.get(partyPermission).getId());
    }
    String upgradeKey = "Upgrades.";
    for(PartyUpgrades partyUpgrade : PartyUpgrades.values()){
      partyFileConfiguration.set(upgradeKey + partyUpgrade.getName().replace(" ", ""), partyUpgrades.get(partyUpgrade));
    }
    partyFileConfiguration.set("PartyName", name);
    partyFileConfiguration.set("PartyUpgradePoints", partyUpgradePoints);
    partyFileConfiguration.set("PartyExp", partyExp);
    partyFileConfiguration.set("PartyLevel", partyLevel);
    //Save the public party bank
    for(int i = 0; i < partyBank.getSize(); i++){
      ItemStack item = partyBank.getItem(i);
      if(item != null && item.getType() != Material.AIR){
        partyFileConfiguration.set("PartyBank.Item" + i, item);
      }
    }
    //Save the private bank
    for(int i = 0; i < PartyUpgrades.getPrivateBankSizeAtTier(getUpgradeTier(PartyUpgrades.PRIVATE_BANK_SIZE)); i++){
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
