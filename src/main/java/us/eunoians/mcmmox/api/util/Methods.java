package us.eunoians.mcmmox.api.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcmmox.gui.GUIItem;
import us.eunoians.mcmmox.types.Skills;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class Methods {

  /**
   *
   * @param number The numerical representation of a tier
   * @return The string numeral
   */
  public static String convertToNumeral(int number) {
    switch (number) {
      case 1:
        return "I";
      case 2:
        return "II";
      case 3:
        return "III";
      case 4:
        return "IV";
      case 5:
        return "V";
      default:
        return null;
    }
  }

  /**
   *
   * @param numeral The numeral to convert
   * @return The integer representation of the numeral
   */
  public static int convertToNumber(String numeral) {
    switch (numeral) {
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
   *
   * @param s String to test
   * @return true if the string is an int or false if not
   */
  public static boolean isInt(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  /**
   *
   * @param s String to test
   * @return true if the string is a long or false if not
   */
  public static boolean isLong(String s) {
	try {
	  Long.parseLong(s);
	} catch (NumberFormatException nfe) {
	  return false;
	}
	return true;
  }

  /**
   *
   * @param msg String to colour
   * @return The coloured string
   */
  public static String color(String msg) {
    return ChatColor.translateAlternateColorCodes('&', msg);
  }

  /**
   *
   * @param minutes The number of minutes to convert
   * @return The ticks equal to minute amount
   */
  public static int convertMinToTicks(int minutes) {
    int ticks = minutes * 1200;
    return ticks;
  }

  /**
   *
   * @param uuid UUID to test
   * @return true if the player has logged in before or false if they have not
   */
  public static boolean hasPlayerLoggedInBefore(UUID uuid) {
    OfflinePlayer targ = Bukkit.getOfflinePlayer(uuid);
    if (!(targ.isOnline() || targ.hasPlayedBefore())) {
      return false;
    } else {
      return true;
    }
  }

  @SuppressWarnings("deprecation")
  public static boolean hasPlayerLoggedInBefore(String playerName) {
    OfflinePlayer targ = Bukkit.getOfflinePlayer(playerName);
    if (!(targ.isOnline() || targ.hasPlayedBefore())) {
      return false;
    } else {
      return true;
    }
  }

  /**
   *
   * @param lore The list of strings to colour
   * @return The list of coloured strings
   */
  public static List<String> colorLore(List<String> lore) {
    for (int i = 0; i < lore.size(); i++) {
      String s = lore.get(i);
      lore.set(i, Methods.color(s));
    }
    return lore;
  }

  /**
   *
   * @param inv The inventory to fill
   * @param filler The item stack to fill air slots with
   * @param items The array list of GUIItems to put in the inventory
   * @return
   */
  public static Inventory fillInventory(Inventory inv, ItemStack filler, ArrayList<GUIItem> items) {
    for (GUIItem item : items) {
      inv.setItem(item.getSlot(), item.getItemStack());
    }
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack testItem = inv.getItem(i);
      if (testItem == null && filler != null) {
        inv.setItem(i, filler);
      }
    }
    return inv;
  }

  /**
   *
   * @return Current time in millis
   */
  public static long getCurrentTimeInMillis() {
    Calendar cal = Calendar.getInstance();
    return cal.getTimeInMillis();
  }

  /**
   *
   * @param type Calendar.TimeUnit
   * @param duration How long we are adding
   * @return The time in millis for end time
   */
  public static long getEndTimeInMillis(int type, int duration) {
    Calendar cal = Calendar.getInstance();
    cal.add(type, duration);
    return cal.getTimeInMillis();
  }

  /**
   *
   * @param item Item stack to test
   * @return The Skill that the item belongs to
   */
  public static Skills getSkillsItem(ItemStack item){
	switch(item.getType()){
	  case DIAMOND_SWORD: return Skills.SWORDS;
	  case IRON_SWORD: return Skills.SWORDS;
	  case GOLDEN_SWORD: return Skills.SWORDS;
	  case STONE_SWORD: return Skills.SWORDS;
	  case WOODEN_SWORD: return Skills.SWORDS;
	}
	return null;
  }
}