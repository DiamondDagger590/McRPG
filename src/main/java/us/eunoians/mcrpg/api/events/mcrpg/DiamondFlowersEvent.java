package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.herbalism.DiamondFlowers;
import us.eunoians.mcrpg.api.util.DiamondFlowersData;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class DiamondFlowersEvent extends AbilityActivateEvent {

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

  public DiamondFlowersEvent(McRPGPlayer player, DiamondFlowers diamondFlowers, DiamondFlowersData.DiamondFlowersItem item){
    super(diamondFlowers, player, AbilityEventType.RECREATIONAL);
    this.exp = item.getExp();
    this.maxAmount = item.getMaxAmount();
    this.minAmount = item.getMinAmount();
    this.material = item.getMaterial();
  }
}
