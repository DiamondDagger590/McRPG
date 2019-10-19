package us.eunoians.mcrpg.api.util.brewing;

import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;

public class PotionUtils {

  public static boolean canPotionAcceptIngredient(BasePotion basePotion, ItemStack ingredient){
    return false;
  }

  public static boolean isFuel(ItemStack fuelItem){
    return McRPG.getInstance().getPotionRecipeManager().isFuel(fuelItem);
  }
}
