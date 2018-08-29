package us.eunoians.mcmmox.api.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcmmox.gui.GUIItem;
import us.eunoians.mcmmox.players.McMMOPlayer;

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

  public static boolean isDouble(String s) {
    try {
      Double.parseDouble(s);
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

  public static Inventory parseGUILore(Inventory inv) {
    Inventory newInv = Bukkit.createInventory(null, inv.getSize(), inv.getTitle());
    for (int x = 0; x < inv.getSize(); x++) {
      ItemStack i = inv.getItem(x);
      List<String> newLore = new ArrayList<String>();
      if (i.hasItemMeta() && i.getItemMeta().hasLore()) {
        List<String> lore = i.getItemMeta().getLore();
                /*for(String s : lore) {
                    s = s.replaceAll("%NormalAmount%", Integer.toString(p.getNormalBoosterAmount(type)));
                    s = s.replaceAll("%LuckyAmount%", Integer.toString(p.getLuckyBoosterAmount(type)));
                    newLore.add(s);
                }*/
        ItemMeta meta = i.getItemMeta();
        meta.setLore(newLore);
        i.setItemMeta(meta);
        newInv.setItem(x, i);
      } else {
        newInv.setItem(x, i);
      }
    }
    return newInv;
  }

  public static String replacePlaceHolders(String s, McMMOPlayer player) {
    return s.replaceAll("%Player%", player.getOfflineMcMMOPlayer().getName()).replaceAll("%Ability_Points%", Integer.toString(player.getAbilityPoints()))
            .replaceAll("%Power_Level%", Integer.toString(player.getPowerLevel()));
  }
}