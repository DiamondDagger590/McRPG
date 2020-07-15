package us.eunoians.mcrpg.api.util;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.gui.GUIItem;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.party.PartyMember;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.PartyPermissions;
import us.eunoians.mcrpg.types.Skills;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class Methods{
  
  private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();
  
  static{
    
    map.put(1000, "M");
    map.put(900, "CM");
    map.put(500, "D");
    map.put(400, "CD");
    map.put(100, "C");
    map.put(90, "XC");
    map.put(50, "L");
    map.put(40, "XL");
    map.put(10, "X");
    map.put(9, "IX");
    map.put(5, "V");
    map.put(4, "IV");
    map.put(1, "I");
    
  }
  
  public static String convertToNumeral(int number){
    int l = map.floorKey(number);
    if(number == l){
      return map.get(number);
    }
    return map.get(l) + convertToNumeral(number - l);
  }
  
  
  /**
   * @param numeral The numeral to convert
   * @return The integer representation of the numeral
   */
  public static int convertToNumber(String numeral){
    switch(numeral){
      case "I":
        return 1;
      case "II":
        return 2;
      case "III":
        return 3;
      case "IV":
        return 4;
      case "V":
        return 5;
      default:
        return 0;
    }
  }
  
  /**
   * @param s String to test
   * @return true if the string is an int or false if not
   */
  public static boolean isInt(String s){
    try{
      Integer.parseInt(s);
    }catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }
  
  /**
   * @param s String to test
   * @return true if the string is a long or false if not
   */
  public static boolean isLong(String s){
    try{
      Long.parseLong(s);
    }catch(NumberFormatException nfe){
      return false;
    }
    return true;
  }
  
  /**
   * @param msg String to colour
   * @return The coloured string
   */
  public static String color(String msg){
    return ChatColor.translateAlternateColorCodes('&', msg);
  }
  
  public static String color(Player p, String msg){
    if(McRPG.getInstance().isPapiEnabled()){
      msg = PlaceholderAPI.setPlaceholders(p, msg);
    }
    return ChatColor.translateAlternateColorCodes('&', msg);
    
  }
  
  /**
   * @param minutes The number of minutes to convert
   * @return The ticks equal to minute amount
   */
  public static int convertMinToTicks(int minutes){
    int ticks = minutes * 1200;
    return ticks;
  }
  
  /**
   * @param uuid UUID to test
   * @return true if the player has logged in before or false if they have not
   */
  public static boolean hasPlayerLoggedInBefore(UUID uuid){
    OfflinePlayer targ = Bukkit.getOfflinePlayer(uuid);
    if(!(targ.isOnline() || targ.hasPlayedBefore())){
      return false;
    }
    else{
      return true;
    }
  }
  
  @SuppressWarnings("deprecation")
  public static boolean hasPlayerLoggedInBefore(String playerName){
    OfflinePlayer targ = Bukkit.getOfflinePlayer(playerName);
    if(!(targ.isOnline() || targ.hasPlayedBefore())){
      return false;
    }
    else{
      return true;
    }
  }
  
  /**
   * @param lore The list of strings to colour
   * @return The list of coloured strings
   */
  public static List<String> colorLore(List<String> lore){
    for(int i = 0; i < lore.size(); i++){
      String s = lore.get(i);
      lore.set(i, Methods.color(s));
    }
    return lore;
  }
  
  /**
   * @param inv    The inventory to fill
   * @param filler The item stack to fill air slots with
   * @param items  The array list of GUIItems to put in the inventory
   * @return
   */
  public static Inventory fillInventory(Inventory inv, ItemStack filler, List<GUIItem> items){
    for(GUIItem item : items){
      if(item.getItemStack() == null){
        continue;
      }
      inv.setItem(item.getSlot(), item.getItemStack());
    }
    for(int i = 0; i < inv.getSize(); i++){
      ItemStack testItem = inv.getItem(i);
      if(testItem == null && filler != null){
        inv.setItem(i, filler);
      }
    }
    return inv;
  }
  
  /**
   * @return Current time in millis
   */
  public static long getCurrentTimeInMillis(){
    Calendar cal = Calendar.getInstance();
    return cal.getTimeInMillis();
  }
  
  /**
   * @param type     Calendar.TimeUnit
   * @param duration How long we are adding
   * @return The time in millis for end time
   */
  public static long getEndTimeInMillis(int type, int duration){
    Calendar cal = Calendar.getInstance();
    cal.add(type, duration);
    return cal.getTimeInMillis();
  }
  
  /**
   * @param m Material
   * @return The Skill that the item belongs to
   */
  public static Skills getSkillsItem(Material m, Material blockType){
    switch(m.name()){
      case "NETHERITE_SWORD":
      case "DIAMOND_SWORD":
      case "IRON_SWORD":
      case "GOLDEN_SWORD":
      case "STONE_SWORD":
      case "WOODEN_SWORD":
        return Skills.SWORDS;
      case "NETHERITE_PICKAXE":
      case "DIAMOND_PICKAXE":
      case "IRON_PICKAXE":
      case "GOLDEN_PICKAXE":
      case "STONE_PICKAXE":
      case "WOODEN_PICKAXE":
        return Skills.MINING;
      case "NETHERITE_HOE":
      case "DIAMOND_HOE":
      case "IRON_HOE":
      case "GOLDEN_HOE":
      case "STONE_HOE":
      case "WOODEN_HOE":
        return Skills.HERBALISM;
      case "NETHERITE_AXE":
      case "DIAMOND_AXE":
      case "IRON_AXE":
      case "GOLDEN_AXE":
      case "STONE_AXE":
      case "WOODEN_AXE":
        switch(blockType){
          case AIR:
            return Skills.AXES;
        }
        return Skills.WOODCUTTING;
      case "BOW":
        return Skills.ARCHERY;
      case "NETHERITE_SHOVEL":
      case "DIAMOND_SHOVEL":
      case "IRON_SHOVEL":
      case "GOLDEN_SHOVEL":
      case "STONE_SHOVEL":
      case "WOODEN_SHOVEL":
        return Skills.EXCAVATION;
      case "BLAZE_ROD":
        return Skills.TAMING;
      case "AIR":
        return Skills.UNARMED;
    }
    return null;
  }
  
  public static boolean specialHandDigggingCase(Material material){
    switch(material){
      case DIRT:
      case COARSE_DIRT:
      case GRASS_BLOCK:
      case GRASS_PATH:
      case FARMLAND:
      case MYCELIUM:
      case PODZOL:
      case GRAVEL:
      case SAND:
      case RED_SAND:
        return true;
    }
    return false;
  }
  
  public static Location lookAt(Location loc, Location lookat){
    //Clone the loc to prevent applied changes to the input loc
    loc = loc.clone();
    
    // Values of change in distance (make it relative)
    double dx = lookat.getX() - loc.getX();
    double dy = lookat.getY() - loc.getY();
    double dz = lookat.getZ() - loc.getZ();
    
    // Set yaw
    if(dx != 0){
      // Set yaw start value based on dx
      if(dx < 0){
        loc.setYaw((float) (1.5 * Math.PI));
      }
      else{
        loc.setYaw((float) (0.5 * Math.PI));
      }
      loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
    }
    else if(dz < 0){
      loc.setYaw((float) Math.PI);
    }
    
    // Get the distance from dx/dz
    double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
    
    // Set pitch
    loc.setPitch((float) -Math.atan(dy / dxz));
    
    // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
    loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
    loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);
    
    return loc;
  }
  
  public static void setMetadata(Entity e, String key, Object value){
    e.setMetadata(key, new FixedMetadataValue(McRPG.getInstance(), value));
  }
  
  /**
   * This converts a location into a string for storage. This method uses ':' as the delimiter
   *
   * @param loc The location being parsed into a string
   * @return The string version of the location
   */
  public static String locToString(Location loc){
    return loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + loc.getWorld().getName();
  }
  
  public static String chunkToLoc(Chunk chunk){
    // use @ instead of : as delimiters here because Windows doesn't like colons in filenames
    return chunk.getX() + "@" + chunk.getZ() + "@" + chunk.getWorld().getName();
  }
  
  /**
   * This parses a string back into a location. This uses ':' as the delimiter
   *
   * @param loc The string that is being turned into a location
   * @return The location that was stored as a string
   */
  public static Location stringToLoc(String loc){
    String[] args = loc.split(":");
    World w = Bukkit.getWorld(args[3]);
    return new Location(w, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
  }
  
  /**
   * This converts a name to an SQL friendly format
   *
   * @param name The string that is being parsed
   * @return An SQL friendly version of the string
   */
  public static String convertNameToSQL(String name){
    if(name.equalsIgnoreCase("PETASWrath")){
      return "petas_wrath";
    }
    StringBuilder returnVal = new StringBuilder();
    boolean first = true;
    for(String s : name.split("")){
      if(first){
        returnVal.append(s.toLowerCase());
        first = false;
      }
      else{
        if(Character.isUpperCase(s.charAt(0))){
          returnVal.append("_");
        }
        returnVal.append(s.toLowerCase());
      }
    }
    return returnVal.toString();
  }
  
  public static String convertBool(Boolean bool){
    if(bool){
      return "1";
    }
    else{
      return "0";
    }
  }
  
  public static boolean isDiamondFlower(Material material){
    return material == Material.POPPY || material == Material.DANDELION || material == Material.BLUE_ORCHID || material == Material.LILAC;
  }
  
  public static boolean isSkillBook(ItemStack itemStack){
    NBTItem item = new NBTItem(itemStack);
    return item.hasKey("UpgradeSkill") || item.hasKey("UnlockSkill");
  }
  
  public static boolean isArtifact(ItemStack itemStack){
    if(itemStack == null || itemStack.getType() == Material.AIR){
      return false;
    }
    NBTItem item = new NBTItem(itemStack);
    return item.getBoolean("McRPGArtifact");
  }
  
  //Following three methods is from the following thread: https://www.spigotmc.org/threads/how-to-get-players-exp-points.239171/#post-2406336
  
  // Calculate amount of EXP needed to level up
  public static int getExpToLevelUp(int level){
    if(level <= 15){
      return 2 * level + 7;
    }
    else if(level <= 30){
      return 5 * level - 38;
    }
    else{
      return 9 * level - 158;
    }
  }
  
  // Calculate total experience up to a level
  public static int getExpAtLevel(int level){
    if(level <= 16){
      return (int) (Math.pow(level, 2) + 6 * level);
    }
    else if(level <= 31){
      return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360.0);
    }
    else{
      return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220.0);
    }
  }
  
  // Calculate player's current EXP amount
  public static int getPlayerExp(Player player){
    int exp = 0;
    int level = player.getLevel();
    
    // Get the amount of XP in past levels
    exp += getExpAtLevel(level);
    
    // Get amount of XP towards next level
    exp += Math.round(getExpToLevelUp(level) * player.getExp());
    
    return exp;
  }
  
  public static int findHoursDiffFromCurrent(long time){
    Calendar calendar = Calendar.getInstance();
    long milliseconds = calendar.getTimeInMillis() - time;
    long seconds, minutes, hours;
    seconds = milliseconds / 1000;
    minutes = seconds / 60;
    hours = minutes / 60;
    return (int) hours;
  }
  
  public static String getLastLoginDay(OfflinePlayer offlinePlayer){
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return simpleDateFormat.format(new Date(offlinePlayer.getLastPlayed()));
  }
  
  public static boolean canPlayersPVP(Player player1, Player player2){
    if(!McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getBoolean("PartiesEnabled", false)){
      return true;
    }
    try{
      McRPGPlayer mcRPGPlayer1 = PlayerManager.getPlayer(player1.getUniqueId());
      McRPGPlayer mcRPGPlayer2 = PlayerManager.getPlayer(player2.getUniqueId());
      if(mcRPGPlayer1.getPartyID() != null && mcRPGPlayer2.getPartyID() != null && mcRPGPlayer1.getPartyID().equals(mcRPGPlayer2.getPartyID())){
        Party party = McRPG.getInstance().getPartyManager().getParty(mcRPGPlayer1.getPartyID());
        if(party != null){
          PartyMember partyMember1 = party.getPartyMember(player1.getUniqueId());
          PartyMember partyMember2 = party.getPartyMember(player2.getUniqueId());
          if(partyMember1 != null && partyMember2 != null){
            if(partyMember2.getPartyRole().getId() <= party.getRoleForPermission(PartyPermissions.PVP).getId()){
              return true;
            }
            else{
              return false;
            }
          }
        }
      }
      return true;
    }catch(McRPGPlayerNotFoundException e){
      return true;
    }
  }
  
}