package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.herbalism.MassHarvest;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class MassHarvestEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int range;

  public MassHarvestEvent(McMMOPlayer player, MassHarvest massHarvest, int range){
    super(massHarvest, player);
    this.range = range;
  }
}
