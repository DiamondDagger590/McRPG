package us.eunoians.mcrpg.api.util.brewing;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;

public class PotionUtils {

  public static boolean canPotionAcceptIngredient(BasePotion basePotion, ItemStack ingredient){
    return false;
  }

  public static boolean isFuel(ItemStack fuelItem){
    return McRPG.getInstance().getPotionRecipeManager().isFuel(fuelItem);
  }

  public static boolean isIngredient(ItemStack ingredient){
    return McRPG.getInstance().getPotionRecipeManager().isValidIngredient(ingredient.getType());
  }

  public static boolean isPotionItem(ItemStack potion){
    return potion.getType() == Material.POTION || potion.getType() == Material.SPLASH_POTION || potion.getType() == Material.LINGERING_POTION;
  }
}
