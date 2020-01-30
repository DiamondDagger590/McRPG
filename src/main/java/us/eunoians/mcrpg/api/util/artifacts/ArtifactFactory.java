package us.eunoians.mcrpg.api.util.artifacts;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArtifactFactory{
  
  private static final Random RANDOM = new Random();
  
  public ItemStack generateArtifact(String type){
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
      if(artifactFile.contains(key + "Lore")){
        itemMeta.setLore(Methods.colorLore(artifactFile.getStringList(key + "Lore")));
      }
      if(artifactFile.getBoolean(key + "IsGlowing", true)){
        artifact.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      }
      artifact.setItemMeta(itemMeta);
      NBTItem nbtItem = new NBTItem(artifact);
      nbtItem.setBoolean("McRPGArtifact", true);
      if(effectType.equals("RedeemableExp")){
        String[] data = artifactFile.getString(key + "RedeemableExpRange").split("-");
        int lowEnd = Integer.parseInt(data[0]);
        int highEnd = data.length > 1 ? Integer.parseInt(data[1]) : lowEnd;
        
      }
    }
    return artifact;
  }
}
