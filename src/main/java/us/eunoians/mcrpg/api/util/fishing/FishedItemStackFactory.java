package us.eunoians.mcrpg.api.util.fishing;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.api.util.Methods;

import java.util.*;
import java.util.stream.Collectors;

public class FishedItemStackFactory {

  private static Random rand = new Random();

  public static ItemStack createItem(@NotNull Material mat){
    return new ItemStack(mat);
  }

  public static ItemStack createItem(@NotNull Material mat, @NotNull String displayName){
    ItemStack itemStack = new ItemStack(mat);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(Methods.color(displayName));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack createItem(@NotNull Material mat, @Nullable String displayName, @Nullable List<String> lore){
    if(displayName == null && lore == null){
      return createItem(mat);
    }
    else if(displayName != null && lore == null){
      return createItem(mat, displayName);
    }
    else if(displayName == null){
      return createItem(mat, lore);
    }
    else{
      ItemStack itemStack = new ItemStack(mat);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Methods.color(displayName));
      itemMeta.setLore(Methods.colorLore(lore));
      itemStack.setItemMeta(itemMeta);
      return itemStack;
    }
  }

  public static ItemStack createItem(@NotNull Material mat, @NotNull List<String> lore){
    ItemStack itemStack = new ItemStack(mat);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setLore(Methods.colorLore(lore));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack damageItem(@NotNull ItemStack item, int lowDamage, int highDamage){
    if(item.getItemMeta() instanceof Damageable){
      double damage = lowDamage + rand.nextInt(highDamage - lowDamage);
      Damageable meta = (Damageable) item.getItemMeta();
      double multiplier = (damage/100d);
      damage = (int) (item.getType().getMaxDurability() * multiplier);
      meta.setDamage(item.getType().getMaxDurability() - (int) damage);
      item.setItemMeta((ItemMeta) meta);
    }
    return item;
  }

  public static ItemStack convertToPotion(ItemStack item, PotionMeta potionMeta){
    if(potionMeta.isSplash()){
      item.setType(Material.SPLASH_POTION);
    }
    else if(potionMeta.isLingering()){
      item.setType(Material.LINGERING_POTION);
    }
    else{
      item.setType(Material.POTION);
    }
    org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) item.getItemMeta();
    if(meta != null){
      if(!potionMeta.getRGB().equals("")){
        List<Integer> rgb = Arrays.stream(potionMeta.getRGB().split(":")).map(Integer::parseInt).collect(Collectors.toList());
        meta.setColor(Color.fromRGB(rgb.get(0), rgb.get(1), rgb.get(2)));
      }
      if(potionMeta.isCustomPotion()){
        for(PotionMeta.PotionSubMeta subMeta : potionMeta.getEffects()){
          meta.addCustomEffect(new PotionEffect(subMeta.getEffectType(), subMeta.getDuration() * 20, subMeta.getLevel() - 1), false);
        }
      }
      else{
        meta.setBasePotionData(new PotionData(potionMeta.getPotionType(), potionMeta.isExtended(), potionMeta.isUpgraded()));
      }
    }
    item.setItemMeta(meta);
    return item;
  }

  public static ItemStack enchantItem(@NotNull ItemStack item, @NotNull EnchantmentMeta enchantmentMeta){
    Map<Enchantment, Integer> enchants = enchantmentMeta.generateEnchantmentMap();
    if(item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK){
      EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) item.getItemMeta();
      boolean override = enchantmentMeta.isOverrideExistingEnchants();
      if(override && enchantmentStorageMeta != null){
        List<Enchantment> toRemove = new ArrayList<>(enchantmentStorageMeta.getStoredEnchants().keySet());
        for(Enchantment enchantment : toRemove){
          enchantmentStorageMeta.removeStoredEnchant(enchantment);
        }
      }
      if(enchantmentStorageMeta != null){
        for(Enchantment enchantment : enchants.keySet()){
          enchantmentStorageMeta.addStoredEnchant(enchantment, enchants.get(enchantment), true);
        }
      }
      item.setItemMeta(enchantmentStorageMeta);
    }
    else{
      boolean override = enchantmentMeta.isOverrideExistingEnchants();
      if(override){
        List<Enchantment> toRemove = new ArrayList<>(item.getEnchantments().keySet());
        for(Enchantment enchantment : toRemove){
          item.removeEnchantment(enchantment);
        }
      }
      for(Enchantment enchantment : enchants.keySet()){
        item.addEnchantment(enchantment, enchants.get(enchantment));
      }
    }
    return item;
  }

}