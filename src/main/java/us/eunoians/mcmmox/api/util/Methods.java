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

  public static boolean isInt(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  public static String color(String msg) {
    return ChatColor.translateAlternateColorCodes('&', msg);
  }

  public static int convertMinToTicks(int minutes) {
    int ticks = minutes * 1200;
    return ticks;
  }

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

  public static List<String> colorLore(List<String> lore) {
    for (int i = 0; i < lore.size(); i++) {
      String s = lore.get(i);
      lore.set(i, Methods.color(s));
    }
    return lore;
  }

  public static Inventory fillInventory(Inventory inv, ItemStack filler, ArrayList<GUIItem> items) {
    for (GUIItem item : items) {
      inv.setItem(item.getSlot(), item.getItemStack());
    }
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack testItem = inv.getItem(i);
      if (testItem == null) {
        inv.setItem(i, filler);
      }
    }
    return inv;
  }

  public static long getCurrentTimeInMillis() {
    Calendar cal = Calendar.getInstance();
    return cal.getTimeInMillis();
  }

  public static long getEndTimeInMillis(int type, int duration) {
    Calendar cal = Calendar.getInstance();
    cal.add(type, duration);
    return cal.getTimeInMillis();
  }

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