package us.eunoians.mcrpg.api.util.brewing;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import us.eunoians.mcrpg.types.BasePotionType;

@SuppressWarnings("deprecation")
public class PotionFactory {

  public static BasePotion convertItemStackToBasePotion(ItemStack potion){
    return new BasePotion(potion);
  }

  public static BasePotionType getBasePotionTypeFromItemStack(ItemStack potion){
    if(!potion.getType().name().contains("POTION")){
      return null;
    }
    else{
      PotionMeta meta = (PotionMeta) potion.getItemMeta();
      if(meta.getBasePotionType() == PotionType.WATER){
        return BasePotionType.WATER;
      }
      else if(meta.getBasePotionType() == PotionType.AWKWARD){
        return BasePotionType.AWKWARD;
      }
      else{
        PotionEffectType potionEffectType = meta.getBasePotionType().getEffectType();
        if(meta.getBasePotionType() == null){
          if(meta.hasCustomEffects()){
            potionEffectType = meta.getCustomEffects().get(0).getType();
          }
        }
        return BasePotionType.getFromPotionEffect(potionEffectType);
      }
    }
  }
}
