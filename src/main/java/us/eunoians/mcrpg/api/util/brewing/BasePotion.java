package us.eunoians.mcrpg.api.util.brewing;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.BasePotionType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static us.eunoians.mcrpg.api.util.brewing.PotionFactory.getBasePotionTypeFromItemStack;

public class BasePotion {

  @Getter
  @Setter
  private int totalTimesModified;

  @Getter
  @Setter
  private String tag;

  @Getter
  @Setter
  private BasePotionType basePotionType;

  @Getter
  private ItemStack potionItem;

  private NBTItem nbtItem;

  BasePotion(ItemStack potion){
    PotionMeta meta = (PotionMeta) potion.getItemMeta();
    PotionData data = meta.getBasePotionData();
    PotionType potionType = data.getType();
    if(potionType != PotionType.AWKWARD && potionType != PotionType.WATER && potionType != PotionType.UNCRAFTABLE){
      meta.setBasePotionData(new PotionData(PotionType.UNCRAFTABLE));
      int duration = data.isExtended() ? 480 : data.isUpgraded() ? 90 : potionType.isInstant() ? 0 : 180;
      if(potion.getType() == Material.LINGERING_POTION){
        duration = duration/4;
      }
      int amplifier = data.isUpgraded() ? 1 : 0;
      meta.addCustomEffect(new PotionEffect(data.getType().getEffectType(), duration * 20, amplifier), true);
      potion.setItemMeta(meta);
    }
    this.basePotionType = getBasePotionTypeFromItemStack(potion);
    if(basePotionType != BasePotionType.AWKWARD && basePotionType != BasePotionType.WATER){
      String name = (potion.getType() == Material.LINGERING_POTION ? "Lingering " : potion.getType() == Material.SPLASH_POTION ? "Splash " : "") + "Potion of " + basePotionType.getDisplayName();
      meta.setDisplayName(Methods.color("&f" + name));
      List<Integer> rgb = Arrays.stream(basePotionType.getCustomColour().split(":")).map(Integer::parseInt).collect(Collectors.toList());
      meta.setColor(Color.fromRGB(rgb.get(0), rgb.get(1), rgb.get(2)));
    }
    potion.setItemMeta(meta);
    this.potionItem = potion;
    nbtItem = new NBTItem(potionItem);

    if(nbtItem.hasNBTData()){
      this.tag = nbtItem.hasKey("McRPGTag") ? nbtItem.getString("McRPGTag") : null;
      totalTimesModified = nbtItem.hasKey("TotalModified") ? nbtItem.getInteger("TotalModified") : 0;
      potionItem = nbtItem.getItem();
    }
    else{
      totalTimesModified = 0;
    }
    if(tag == null || tag.equalsIgnoreCase("INVALID")){
      PotionRecipeManager potionRecipeManager = McRPG.getInstance().getPotionRecipeManager();
      if(basePotionType == BasePotionType.WATER){
        this.tag = "Default";
      }
      else if(basePotionType == BasePotionType.AWKWARD){
        this.tag = "Default";
      }
      else{
        if(potionRecipeManager.isPotionTypeRegistered(basePotionType)){
          PotionEffectTagWrapper potionEffectTagWrapper = potionRecipeManager.getPotionEffectTagWrapper(basePotionType);
          Map<String, TagMeta> allTags = potionEffectTagWrapper.getAllTags();
          PotionEffect potionEffect = meta.getCustomEffects().get(0);
          int duration = potionEffect.getDuration()/20;
          int amplifier = potionEffect.getAmplifier();
          int highestWeight = 0;
          String highestTag = "";
          for(String tag : allTags.keySet()){
            TagMeta tagMeta = allTags.get(tag);
            int diffInDuration = Math.abs(tagMeta.getDuration() - duration);
            int diffInAmplifier = Math.abs(tagMeta.getPotionEffectLevel() - amplifier);
            int weight = (180 - diffInDuration) + (200 - (200 * diffInAmplifier));
            if(weight > highestWeight){
              highestTag = tag;
              highestWeight = weight;
            }
          }
          this.tag = highestTag;
        }
      }
    }
    saveStack();
  }

  public void updateInfo(){
    potionItem = nbtItem.getItem();
    totalTimesModified++;
    PotionRecipeManager potionRecipeManager = McRPG.getInstance().getPotionRecipeManager();
    PotionEffectTagWrapper potionEffectTagWrapper = potionRecipeManager.getPotionEffectTagWrapper(basePotionType);
    TagMeta tagMeta = potionEffectTagWrapper.getTagMeta(tag);
    PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
    basePotionType = tagMeta.getBasePotionType();
    Bukkit.broadcastMessage(basePotionType.getName());
    //Set the new base potion data in order to override with the actual potion info
    //This is needed since we can not modify the base potion data's duration but we use it in other parts of the code
    if(basePotionType == BasePotionType.AWKWARD){
      potionMeta.setBasePotionData(new PotionData(PotionType.AWKWARD));
    }
    else{
      potionMeta.setBasePotionData(new PotionData(PotionType.UNCRAFTABLE));
    }
    if(basePotionType != BasePotionType.AWKWARD && basePotionType != BasePotionType.WATER){
      PotionEffect newPotionEffect = new PotionEffect(basePotionType.getEffectType(), tagMeta.getDuration() * 20, tagMeta.getPotionEffectLevel() - 1);
      potionMeta.clearCustomEffects();
      potionMeta.addCustomEffect(newPotionEffect, true);
      String name = (potionItem.getType() == Material.LINGERING_POTION ? "Lingering " : potionItem.getType() == Material.SPLASH_POTION ? "Splash " : "") + "Potion of " + basePotionType.getDisplayName();
      potionMeta.setDisplayName(Methods.color("&f" + name));
      List<Integer> rgb = Arrays.stream(basePotionType.getCustomColour().split(":")).map(Integer::parseInt).collect(Collectors.toList());
      potionMeta.setColor(Color.fromRGB(rgb.get(0), rgb.get(1), rgb.get(2)));
    }
    potionItem.setItemMeta(potionMeta);
    nbtItem = new NBTItem(potionItem);
    saveStack();
  }

  public void setSplash(){
    if(basePotionType == BasePotionType.AWKWARD || basePotionType == BasePotionType.WATER){
      return;
    }
    potionItem = nbtItem.getItem();
    potionItem.setType(Material.SPLASH_POTION);
    double splashModifier = McRPG.getInstance().getPotionRecipeManager().getPotionEffectTagWrapper(basePotionType).getSplashDurationModifier();
    PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
    potionMeta.setDisplayName(Methods.color("&fSplash " + ChatColor.stripColor(potionMeta.getDisplayName())));
    PotionEffect customEffect = potionMeta.getCustomEffects().get(0);
    PotionEffect newEffect = new PotionEffect(basePotionType.getEffectType(), (int) (customEffect.getDuration() * splashModifier), customEffect.getAmplifier());
    potionMeta.clearCustomEffects();
    potionMeta.addCustomEffect(newEffect, true);
    potionItem.setItemMeta(potionMeta);
    nbtItem = new NBTItem(potionItem);
    totalTimesModified++;
    saveStack();
  }

  public void setLingering(){
    if(basePotionType == BasePotionType.AWKWARD || basePotionType == BasePotionType.WATER){
      return;
    }
    potionItem = nbtItem.getItem();
    potionItem.setType(Material.LINGERING_POTION);
    double lingeringModifier = McRPG.getInstance().getPotionRecipeManager().getPotionEffectTagWrapper(basePotionType).getLingeringDurationModifier();
    PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
    potionMeta.setDisplayName(Methods.color("&fLingering Potion of " + basePotionType.getDisplayName()));
    PotionEffect customEffect = potionMeta.getCustomEffects().get(0);
    Bukkit.broadcastMessage(customEffect.getDuration() + " " + customEffect.getDuration() * lingeringModifier);
    PotionEffect newEffect = new PotionEffect(basePotionType.getEffectType(), (int) (customEffect.getDuration() * lingeringModifier), customEffect.getAmplifier());
    potionMeta.clearCustomEffects();
    potionMeta.addCustomEffect(newEffect, true);
    potionItem.setItemMeta(potionMeta);
    nbtItem = new NBTItem(potionItem);
    totalTimesModified++;
    saveStack();
  }

  public void saveStack(){
    nbtItem.setString("McRPGTag", tag);
    nbtItem.setInteger("TotalModified", totalTimesModified);
  }

  public ItemStack getAsItem(){
    return nbtItem.getItem();
  }
}
