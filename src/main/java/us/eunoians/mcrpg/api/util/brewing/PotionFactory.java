package us.eunoians.mcrpg.api.util.brewing;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import us.eunoians.mcrpg.types.BasePotionType;

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
      if(meta.getBasePotionData().getType() == PotionType.WATER){
        return BasePotionType.WATER;
      }
      else if(meta.getBasePotionData().getType() == PotionType.AWKWARD){
        return BasePotionType.AWKWARD;
      }
      else{
        PotionData basePotionData = meta.getBasePotionData();
        PotionEffectType potionEffectType = basePotionData.getType().getEffectType();
        if(basePotionData.getType() == PotionType.UNCRAFTABLE){
          if(meta.hasCustomEffects()){
            potionEffectType = meta.getCustomEffects().get(0).getType();
          }
        }
        return BasePotionType.getFromPotionEffect(potionEffectType);
      }
    }
  }
}
