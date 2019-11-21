package us.eunoians.mcrpg.api.util.brewing;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import us.eunoians.mcrpg.McRPG;

import java.util.HashSet;
import java.util.Set;

public class TagMeta {

  private Set<Material> ingredients = new HashSet<>();
  private Set<String> children = new HashSet<>();
  @Getter @Setter
  private int duration = 180;
  @Getter @Setter
  private int potionEffectLevel = 1;

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
