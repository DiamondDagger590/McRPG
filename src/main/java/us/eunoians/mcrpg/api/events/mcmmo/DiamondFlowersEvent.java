package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.herbalism.DiamondFlowers;
import us.eunoians.mcrpg.api.util.DiamondFlowersData;
import us.eunoians.mcrpg.players.McMMOPlayer;

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

  public DiamondFlowersEvent(McMMOPlayer player, DiamondFlowers diamondFlowers, DiamondFlowersData.DiamondFlowersItem item){
    super(diamondFlowers, player);
    this.exp = item.getExp();
    this.maxAmount = item.getMaxAmount();
    this.minAmount = item.getMinAmount();
    this.material = item.getMaterial();
  }
}
