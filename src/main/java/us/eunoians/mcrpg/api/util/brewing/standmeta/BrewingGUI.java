package us.eunoians.mcrpg.api.util.brewing.standmeta;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.api.util.brewing.BasePotion;

public class BrewingGUI {

  @Getter @Setter
  private ItemStack fuel;

  @Getter
  private BrewingStand holder;

  @Getter @Setter
  private ItemStack ingredient;

  @Getter @Setter
  private int currentFuelLevel;

  @Getter @Setter
  private int currentBrewProgress;

  private ItemStack[] specialRewards;

  private BasePotion[] potionItems;
  
}
