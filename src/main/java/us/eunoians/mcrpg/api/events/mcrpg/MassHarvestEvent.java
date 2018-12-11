package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.herbalism.MassHarvest;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class MassHarvestEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int range;

  public MassHarvestEvent(McRPGPlayer player, MassHarvest massHarvest, int range){
    super(massHarvest, player);
    this.range = range;
  }
}
