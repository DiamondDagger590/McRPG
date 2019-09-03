package us.eunoians.mcrpg.api.util.brewing;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.types.BasePotionType;

public class PotionFactory {

  static BasePotion convertItemStackToBasePotion(ItemStack potion){
    return new BasePotion(potion);
  }

  @Nullable
  static BasePotionType getBasePotionTypeFromItemStack(@NotNull ItemStack potion){
    if(!potion.getType().name().contains("POTION")){
      return null;
    }
    else{
      PotionMeta meta = (PotionMeta) potion.getItemMeta();
      boolean isCustom = meta.getBasePotionData().getType() == PotionType.AWKWARD;
      if(!isCustom){
        return BasePotionType.getFromPotionType(meta.getBasePotionData().getType());
      }
      else{
        if(meta.hasCustomEffects()){
          return BasePotionType.getFromPotionEffect(meta.getCustomEffects().get(0).getType());
        }
        else{
          return null;
        }
      }
    }
  }
}
