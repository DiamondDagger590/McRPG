package us.eunoians.mcrpg.api.events.mcrpg.herbalism;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.herbalism.MassHarvest;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class MassHarvestEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int range;

  public MassHarvestEvent(McRPGPlayer player, MassHarvest massHarvest, int range){
    super(massHarvest, player, AbilityEventType.RECREATIONAL);
    this.range = range;
  }
}
