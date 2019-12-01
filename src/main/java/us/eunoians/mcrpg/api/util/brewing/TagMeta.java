package us.eunoians.mcrpg.api.util.brewing;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;

import java.util.HashSet;
import java.util.Set;

public class TagMeta {

  private Set<Material> ingredients = new HashSet<>();
  private Set<String> children;
  @Getter
  private String tag;
  @Getter @Setter
  private int duration;
  @Getter @Setter
  private int potionEffectLevel;

  public TagMeta(String prevKey, String tag){
    this.tag = tag;
    FileConfiguration potionConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.BREWING_ITEMS_CONFIG);
    String key = prevKey + "." + tag + ".";
    potionConfig.getStringList(key + "Ingredients").stream().map(Material::getMaterial).forEach(m -> ingredients.add(m));
    duration = potionConfig.getInt(key + "Duration", 180);
    potionEffectLevel = potionConfig.getInt(key + "PotionEffectLevel", 1);
    children = potionConfig.contains(key + "ChildTags") ? new HashSet<>(potionConfig.getStringList(key + "ChildTags")) : new HashSet<>();
  }

  public boolean isValidIngredient(Material material){
    return ingredients.contains(material);
  }

  public boolean doesIngredientLeadToChild(Material material){
    PotionRecipeManager potionRecipeManager = McRPG.getInstance().getPotionRecipeManager();
    for(String s : children){

    }
    return false;
  }
}
