package us.eunoians.mcrpg.api.util.brewing;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.BasePotionType;

import java.util.HashSet;
import java.util.Set;

public class TagMeta {

  @Getter
  private Set<Material> ingredients = new HashSet<>();
  @Getter
  private Set<String> children;
  @Getter
  private String tag;
  @Getter @Setter
  private int duration;
  @Getter @Setter
  private int potionEffectLevel;
  @Getter
  private BasePotionType basePotionType;

  TagMeta(String prevKey, String tag, BasePotionType basePotionType){
    this.basePotionType = basePotionType;
    this.tag = tag;
    FileConfiguration potionConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_ITEMS_CONFIG);
    String key = prevKey + "." + tag + ".";
    potionConfig.getStringList(key + "Ingredients").stream().map(Material::getMaterial).forEach(m -> ingredients.add(m));
    duration = potionConfig.getInt(key + "Duration", 0);
    potionEffectLevel = potionConfig.getInt(key + "PotionEffectLevel", 1);
    children = potionConfig.contains(key + "ChildTags") ? new HashSet<>(potionConfig.getStringList(key + "ChildTags")) : new HashSet<>();
  }

  public boolean isValidIngredient(Material material){
    return ingredients.contains(material);
  }

  public boolean doesIngredientLeadToChild(Material material){
    PotionRecipeManager potionRecipeManager = McRPG.getInstance().getPotionRecipeManager();
    for(String s : children){
      TagMeta tagMeta = potionRecipeManager.getPotionEffectTagWrapper(basePotionType).getTagMeta(s);
      if(tagMeta.isValidIngredient(material)){
        return true;
      }
    }
    return false;
  }

  public String getChildTag(Material material){
    PotionRecipeManager potionRecipeManager = McRPG.getInstance().getPotionRecipeManager();
    for(String s : children){
      String[] tagData = s.split("\\.");
      BasePotionType tempType = basePotionType;
      String tagToUse = s;
      if(tagData.length > 1){
        tempType = tagData[0].equals("AWKWARD") ? BasePotionType.AWKWARD : BasePotionType.getFromPotionEffect(PotionEffectType.getByName(tagData[0]));
        tagToUse = tagData[1];
      }
      if(tagData.length == 3){
        Material branchMaterial = Material.getMaterial(tagData[2]);
        if(material == branchMaterial){
          return s;
        }
        return null;
      }
      TagMeta tagMeta = potionRecipeManager.getPotionEffectTagWrapper(tempType).getTagMeta(tagToUse);
      if(tagMeta.isValidIngredient(material)){
        return s;
      }
    }
    return null;
  }
}
