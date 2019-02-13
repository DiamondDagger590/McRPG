package us.eunoians.mcrpg.api.events.mcrpg.herbalism;

import lombok.Getter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.herbalism.TooManyPlants;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class TooManyPlantsEvent extends AbilityActivateEvent {

  @Getter
  private Material plantType;

  public TooManyPlantsEvent(McRPGPlayer player, TooManyPlants tooManyPlants, Material plantType){
    super(tooManyPlants, player, AbilityEventType.RECREATIONAL);
    this.plantType = plantType;
  }
}
