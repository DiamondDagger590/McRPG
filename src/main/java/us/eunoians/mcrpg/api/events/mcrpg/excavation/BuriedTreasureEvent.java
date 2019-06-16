package us.eunoians.mcrpg.api.events.mcrpg.excavation;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.excavation.BuriedTreasure;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.api.util.BuriedTreasureData;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class BuriedTreasureEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int exp;

  @Getter
  @Setter
  private int maxAmount;

  @Getter
  @Setter
  private int minAmount;

  @Getter
  @Setter
  private Material material;

  public BuriedTreasureEvent(McRPGPlayer player, BuriedTreasure buriedTreasure, BuriedTreasureData.BuriedTreasureItem item) {
    super(buriedTreasure, player, AbilityEventType.RECREATIONAL);
    this.exp = item.getExp();
    this.maxAmount = item.getMaxAmount();
    this.minAmount = item.getMinAmount();
    this.material = item.getMaterial();
  }
}
