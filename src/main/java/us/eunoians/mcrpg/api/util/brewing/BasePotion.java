package us.eunoians.mcrpg.api.util.brewing;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import us.eunoians.mcrpg.types.BasePotionType;

import static us.eunoians.mcrpg.api.util.brewing.PotionFactory.getBasePotionTypeFromItemStack;

public class BasePotion {

  @Getter @Setter
  private boolean isSplash;

  @Getter @Setter
  private boolean isLingering;

  @Getter
  private BasePotionType basePotionType;

  @Getter
  private ItemStack potionItem;

  @Getter
  private int timesExtended = 0;
  @Getter @Setter
  private int currentExtendedAmount = 0;
  @Getter
  private int timesAmplified = 0;
  @Getter @Setter
  private int currentAmplifiedAmount = 0;
  @Getter
  private int totalTimesModified = 0;
  private NBTItem nbtItem;

  BasePotion(ItemStack potion){
    this.basePotionType = getBasePotionTypeFromItemStack(potion);
    this.isSplash = potion.getType() == Material.SPLASH_POTION;
    this.isLingering = potion.getType() == Material.LINGERING_POTION;
    this.potionItem = potion;
    nbtItem = new NBTItem(potionItem);
    PotionMeta meta = (PotionMeta) potion.getItemMeta();
    PotionData data = meta != null ? meta.getBasePotionData() : null;
    if(nbtItem.hasNBTData()){
      currentAmplifiedAmount = nbtItem.hasKey("CurrentAmplified") ? nbtItem.getInteger("CurrentAmplified") : data.isUpgraded() ? 1 : 0;
      currentExtendedAmount = nbtItem.hasKey("CurrentExtended") ? nbtItem.getInteger("CurrentExtended") : data.isExtended() ? 1 : 0;
      timesAmplified = nbtItem.hasKey("TimesAmplified") ? nbtItem.getInteger("TimesAmplified") : currentAmplifiedAmount;
      timesExtended = nbtItem.hasKey("TimesExtended") ? nbtItem.getInteger("TimesExtended") : currentExtendedAmount;
      totalTimesModified = nbtItem.hasKey("TotalModified") ? nbtItem.getInteger("TotalModified") : timesAmplified + timesExtended;
      potionItem = nbtItem.getItem();
    }
    else{
      if(data != null){
        timesAmplified = data.isUpgraded() ? 1 : 0;
        timesExtended = data.isExtended() ? 1 : 0;
        currentAmplifiedAmount = timesAmplified;
        currentExtendedAmount = timesExtended;
        totalTimesModified = timesAmplified + timesExtended;
      }
    }
    saveStack();
  }

  public void saveStack(){
    nbtItem.setInteger("CurrentAmplified", currentAmplifiedAmount);
    nbtItem.setInteger("CurrentExtended", currentExtendedAmount);
    nbtItem.setInteger("TimesAmplified", timesAmplified);
    nbtItem.setInteger("TimesExtended", timesExtended);
    nbtItem.setInteger("TotalModified", totalTimesModified);
  }

  public ItemStack getAsItem(){
    return nbtItem.getItem();
  }
}
