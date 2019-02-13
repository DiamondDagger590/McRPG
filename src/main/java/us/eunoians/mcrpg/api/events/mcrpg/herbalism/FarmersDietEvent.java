package us.eunoians.mcrpg.api.events.mcrpg.herbalism;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.herbalism.FarmersDiet;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class FarmersDietEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int foodRestorationBonus;

  @Getter
  @Setter
  private double saturationBonus;

  @Getter
  @Setter
  private Material foodItem;

  public FarmersDietEvent(McRPGPlayer player, FarmersDiet farmersDiet, int foodRestorationBonus, double saturationBonus, Material foodItem){
    super(farmersDiet, player, AbilityEventType.RECREATIONAL);
    this.foodRestorationBonus = foodRestorationBonus;
    this.saturationBonus = saturationBonus;
    this.foodItem = foodItem;
  }
}
