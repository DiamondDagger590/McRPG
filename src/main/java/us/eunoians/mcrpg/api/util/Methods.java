package us.eunoians.mcrpg.api.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.gui.GUIItem;
import us.eunoians.mcrpg.types.Skills;

import java.util.*;

public class Methods {

  private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

  static {

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

  public static String convertToNumeral(int number) {
    int l = map.floorKey(number);
    if(number == l) {
      return map.get(number);
    }
    return map.get(l) + convertToNumeral(number - l);
  }


  /**
   * @param numeral The numeral to convert
   * @return The integer representation of the numeral
   */
  public static int convertToNumber(String numeral) {
    switch(numeral) {
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
  public static boolean isInt(String s) {
    try {
      Integer.parseInt(s);
    } catch(NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  /**
   * @param s String to test
   * @return true if the string is a long or false if not
   */
  public static boolean isLong(String s) {
    try {
      Long.parseLong(s);
    } catch(NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  /**
   * @param msg String to colour
   * @return The coloured string
   */
  public static String color(String msg) {
    return ChatColor.translateAlternateColorCodes('&', msg);
  }

  public static String color(Player p, String msg) {
    if(McRPG.getInstance().isPapiEnabled()) {
      msg = PlaceholderAPI.setPlaceholders(p, msg);
    }
    return ChatColor.translateAlternateColorCodes('&', msg);

  }

  /**
   * @param minutes The number of minutes to convert
   * @return The ticks equal to minute amount
   */
  public static int convertMinToTicks(int minutes) {
    int ticks = minutes * 1200;
    return ticks;
  }

  /**
   * @param uuid UUID to test
   * @return true if the player has logged in before or false if they have not
   */
  public static boolean hasPlayerLoggedInBefore(UUID uuid) {
    OfflinePlayer targ = Bukkit.getOfflinePlayer(uuid);
    if(!(targ.isOnline() || targ.hasPlayedBefore())) {
      return false;
    }
    else {
      return true;
    }
  }

  @SuppressWarnings("deprecation")
  public static boolean hasPlayerLoggedInBefore(String playerName) {
    OfflinePlayer targ = Bukkit.getOfflinePlayer(playerName);
    if(!(targ.isOnline() || targ.hasPlayedBefore())) {
      return false;
    }
    else {
      return true;
    }
  }

  /**
   * @param lore The list of strings to colour
   * @return The list of coloured strings
   */
  public static List<String> colorLore(List<String> lore) {
    for(int i = 0; i < lore.size(); i++) {
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
  public static Inventory fillInventory(Inventory inv, ItemStack filler, ArrayList<GUIItem> items) {
    for(GUIItem item : items) {
      if(item.getItemStack() == null) {
        continue;
      }
      inv.setItem(item.getSlot(), item.getItemStack());
    }
    for(int i = 0; i < inv.getSize(); i++) {
      ItemStack testItem = inv.getItem(i);
      if(testItem == null && filler != null) {
        inv.setItem(i, filler);
      }
    }
    return inv;
  }

  /**
   * @return Current time in millis
   */
  public static long getCurrentTimeInMillis() {
    Calendar cal = Calendar.getInstance();
    return cal.getTimeInMillis();
  }

  /**
   * @param type     Calendar.TimeUnit
   * @param duration How long we are adding
   * @return The time in millis for end time
   */
  public static long getEndTimeInMillis(int type, int duration) {
    Calendar cal = Calendar.getInstance();
    cal.add(type, duration);
    return cal.getTimeInMillis();
  }

  /**
   * @param m Material
   * @return The Skill that the item belongs to
   */
  public static Skills getSkillsItem(Material m) {
    switch(m) {
      case DIAMOND_SWORD:
      case IRON_SWORD:
      case GOLDEN_SWORD:
      case STONE_SWORD:
      case WOODEN_SWORD:
        return Skills.SWORDS;
      case DIAMOND_PICKAXE:
      case IRON_PICKAXE:
      case GOLDEN_PICKAXE:
      case STONE_PICKAXE:
      case WOODEN_PICKAXE:
        return Skills.MINING;
      case DIAMOND_HOE:
      case IRON_HOE:
      case GOLDEN_HOE:
      case STONE_HOE:
      case WOODEN_HOE:
        return Skills.HERBALISM;
      case DIAMOND_AXE:
      case IRON_AXE:
      case GOLDEN_AXE:
      case STONE_AXE:
      case WOODEN_AXE:
        return Skills.WOODCUTTING;
      case BOW:
        return Skills.ARCHERY;
      case DIAMOND_SHOVEL:
      case IRON_SHOVEL:
      case GOLDEN_SHOVEL:
      case STONE_SHOVEL:
      case WOODEN_SHOVEL:
        return Skills.EXCAVATION;
      case AIR:
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

  public static Location lookAt(Location loc, Location lookat) {
    //Clone the loc to prevent applied changes to the input loc
    loc = loc.clone();

    // Values of change in distance (make it relative)
    double dx = lookat.getX() - loc.getX();
    double dy = lookat.getY() - loc.getY();
    double dz = lookat.getZ() - loc.getZ();

    // Set yaw
    if(dx != 0) {
      // Set yaw start value based on dx
      if(dx < 0) {
        loc.setYaw((float) (1.5 * Math.PI));
      }
      else {
        loc.setYaw((float) (0.5 * Math.PI));
      }
      loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
    }
    else if(dz < 0) {
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

  public static void setMetadata(Entity e, String key, Object value) {
    e.setMetadata(key, new FixedMetadataValue(McRPG.getInstance(), value));

  }

  public static String locToString(Location loc) {
    return loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + loc.getWorld().getName();
  }

  public static Location stringToLoc(String loc) {
    String[] args = loc.split(":");
    World w = Bukkit.getWorld(args[3]);
    return new Location(w, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
  }

  public static String convertNameToSQL(String name){
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
}