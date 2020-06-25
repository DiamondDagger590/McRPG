package us.eunoians.mcrpg.api.util.blood;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.artifacts.ArtifactManager;
import us.eunoians.mcrpg.types.Skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BloodFactory{
  
  private static final Random RANDOM = new Random();
  
  public static ItemStack generateBlood(String type){
    BloodManager bloodManager = BloodManager.getInstance();
    ItemStack artifact = null;
    if(artifactManager.isArtifactTypeValid(type)){
      FileConfiguration artifactFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ARTIFACT_FILE);
      String key = type + ".Effects.";
      List<String> validTypes = new ArrayList<>();
      while(validTypes.isEmpty()){
        for(String effectType : artifactFile.getConfigurationSection(type + ".Effects").getKeys(false)){
          String newKey = key + effectType + ".";
          if(artifactFile.getBoolean(newKey + "Enabled")){
            double chance = artifactFile.getDouble(newKey + "Chance");
            int val = (int) (chance * 1000);
            if(val >= RANDOM.nextInt(100000)){
              validTypes.add(effectType);
            }
          }
        }
      }
      String effectType = validTypes.get(RANDOM.nextInt(validTypes.size()));
      key += (effectType + ".");
      artifact = new ItemStack(Material.getMaterial(artifactFile.getString(key + "Material", "GOLD_INGOT")));
      ItemMeta itemMeta = artifact.getItemMeta();
      if(artifactFile.contains(key + "DisplayName")){
        itemMeta.setDisplayName(Methods.color(artifactFile.getString(key + "DisplayName")));
      }
      List<String> newLore = new ArrayList<>();
      if(artifactFile.getBoolean(key + "IsGlowing", true)){
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      }
      artifact.setItemMeta(itemMeta);
      if(effectType.equals("RedeemableExp")){
        String[] data = artifactFile.getString(key + "RedeemableExpRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%RedeemableExpAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("RedeemableExpAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("SkillSpecificExp")){
        String[] data = artifactFile.getString(key + "ExpRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        List<Skills> skills = Arrays.asList(Skills.values());
        for(String skill : artifactFile.getStringList(key + "ExcludedSkills")){
          Skills skillType = Skills.fromString(skill);
          skills.remove(skillType);
        }
        Skills skillToUse = skills.get(RANDOM.nextInt(skills.size()));
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%SkillExpAmount%", Integer.toString(amount)).replace("%Skill%", skillToUse.getDisplayName())));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("SkillExpAmount", amount);
        nbtItem.setString("SkillToUse", skillToUse.getName());
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("RedeemableLevel")){
        String[] data = artifactFile.getString(key + "LevelRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%RedeemableLevelAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("RedeemableLevelAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("AbilityPoint")){
        String[] data = artifactFile.getString(key + "AmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%AbilityPointAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("AbilityPointAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("UnlockBookSummon")){
        String[] data = artifactFile.getString(key + "AmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%UnlockBookAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("UnlockBookAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("UpgradeBookSummon")){
        String[] data = artifactFile.getString(key + "AmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%UpgradeBookAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("UpgradeBookAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("CooldownReset")){
        String[] data = artifactFile.getString(key + "UseAmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%RemainingUsesAmount%", Integer.toString(amount)).replace("%MaxUsesAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setBoolean("CooldownReset", true);
        nbtItem.setString("CreationType", type);
        nbtItem.setInteger("MaxUseAmount", amount);
        nbtItem.setInteger("RemainingUseAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("Magnet")){
        String[] data = artifactFile.getString(key + "UseAmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        String[] rangeData = artifactFile.getString(key + "MagnetRadiusRange").split("-");
        int lowEndRange = Integer.parseInt(rangeData[0]);
        int highEndRange = rangeData.length > 1 ? Integer.parseInt(rangeData[1]) : lowEndRange;
        int rangeAmount = lowEndRange + (highEndRange != lowEndRange ? RANDOM.nextInt(highEndRange - lowEndRange) : 0);
        
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%RemainingMagnetUses%", Integer.toString(amount))
                                      .replace("%MaxMagnetUses%", Integer.toString(amount)).replace("%MagnetRadius%", Integer.toString(rangeAmount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("MaxUseAmount", amount);
        nbtItem.setInteger("RemainingUseAmount", amount);
        nbtItem.setInteger("MagnetRange", rangeAmount);
        nbtItem.setString("CreationType", type);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      if(artifactFile.getBoolean(key + "BroadcastOnSpawn")){
        for(Player p : Bukkit.getOnlinePlayers()){
          p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix()
                                        + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.ArtifactCreated")
                                            .replace("%ArtifactType%", artifactFile.getString(key + "BroadcastPlaceHolder"))));
        }
      }
    }
    return artifact;
  }
  
  public static ItemStack generateArtifact(String type, boolean muteAnnounce){
    ArtifactManager artifactManager = ArtifactManager.getInstance();
    ItemStack artifact = null;
    if(artifactManager.isArtifactTypeValid(type)){
      FileConfiguration artifactFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ARTIFACT_FILE);
      String key = type + ".Effects.";
      List<String> validTypes = new ArrayList<>();
      while(validTypes.isEmpty()){
        for(String effectType : artifactFile.getConfigurationSection(type + ".Effects").getKeys(false)){
          String newKey = key + effectType + ".";
          if(artifactFile.getBoolean(newKey + "Enabled")){
            double chance = artifactFile.getDouble(newKey + "Chance");
            int val = (int) (chance * 1000);
            if(val >= RANDOM.nextInt(100000)){
              validTypes.add(effectType);
            }
          }
        }
      }
      String effectType = validTypes.get(RANDOM.nextInt(validTypes.size()));
      key += (effectType + ".");
      artifact = new ItemStack(Material.getMaterial(artifactFile.getString(key + "Material", "GOLD_INGOT")));
      ItemMeta itemMeta = artifact.getItemMeta();
      if(artifactFile.contains(key + "DisplayName")){
        itemMeta.setDisplayName(Methods.color(artifactFile.getString(key + "DisplayName")));
      }
      List<String> newLore = new ArrayList<>();
      if(artifactFile.getBoolean(key + "IsGlowing", true)){
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      }
      artifact.setItemMeta(itemMeta);
      if(effectType.equals("RedeemableExp")){
        String[] data = artifactFile.getString(key + "RedeemableExpRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%RedeemableExpAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("RedeemableExpAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("SkillSpecificExp")){
        String[] data = artifactFile.getString(key + "ExpRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        List<Skills> skills = Arrays.asList(Skills.values());
        for(String skill : artifactFile.getStringList(key + "ExcludedSkills")){
          Skills skillType = Skills.fromString(skill);
          skills.remove(skillType);
        }
        Skills skillToUse = skills.get(RANDOM.nextInt(skills.size()));
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%SkillExpAmount%", Integer.toString(amount)).replace("%Skill%", skillToUse.getDisplayName())));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("SkillExpAmount", amount);
        nbtItem.setString("SkillToUse", skillToUse.getName());
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("RedeemableLevel")){
        String[] data = artifactFile.getString(key + "LevelRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%RedeemableLevelAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("RedeemableLevelAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("AbilityPoint")){
        String[] data = artifactFile.getString(key + "AmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%AbilityPointAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("AbilityPointAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("UnlockBookSummon")){
        String[] data = artifactFile.getString(key + "AmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%UnlockBookAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("UnlockBookAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("UpgradeBookSummon")){
        String[] data = artifactFile.getString(key + "AmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%UpgradeBookAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("UpgradeBookAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("CooldownReset")){
        String[] data = artifactFile.getString(key + "UseAmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%RemainingUsesAmount%", Integer.toString(amount)).replace("%MaxUsesAmount%", Integer.toString(amount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setBoolean("CooldownReset", true);
        nbtItem.setString("CreationType", type);
        nbtItem.setInteger("MaxUseAmount", amount);
        nbtItem.setInteger("RemainingUseAmount", amount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      else if(effectType.equals("Magnet")){
        String[] data = artifactFile.getString(key + "UseAmountRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        int amount = lowEnd + (highEnd != lowEnd ? RANDOM.nextInt(highEnd - lowEnd) : 0);
        String[] rangeData = artifactFile.getString(key + "MagnetRadiusRange").split("-");
        int lowEndRange = Integer.parseInt(rangeData[0]);
        int highEndRange = rangeData.length > 1 ? Integer.parseInt(rangeData[1]) : lowEndRange;
        int rangeAmount = lowEndRange + (highEndRange != lowEndRange ? RANDOM.nextInt(highEndRange - lowEndRange) : 0);
        
        for(String s : artifactFile.getStringList(key + "Lore")){
          newLore.add(Methods.color(s.replace("%RemainingMagnetUses%", Integer.toString(amount))
                                      .replace("%MaxMagnetUses%", Integer.toString(amount)).replace("%MagnetRadius%", Integer.toString(rangeAmount))));
        }
        itemMeta.setLore(newLore);
        artifact.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(artifact);
        nbtItem.setInteger("MaxUseAmount", amount);
        nbtItem.setInteger("RemainingUseAmount", amount);
        nbtItem.setInteger("MagnetRange", rangeAmount);
        nbtItem.setBoolean("McRPGArtifact", true);
        artifact = nbtItem.getItem();
      }
      if(!muteAnnounce && artifactFile.getBoolean(key + "BroadcastOnSpawn")){
        for(Player p : Bukkit.getOnlinePlayers()){
          p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix()
                                        + McRPG.getInstance().getLangFile().getString("Messages.Misc.ArtifactCreated")
                                            .replace("%ArtifactType%", artifactFile.getString(key + "BroadcastPlaceHolder"))));
        }
      }
    }
    return artifact;
  }
}
