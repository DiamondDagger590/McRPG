package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.herbalism.TooManyPlants;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class TooManyPlantsEvent extends AbilityActivateEvent {

  @Getter
  private Material plantType;

  public TooManyPlantsEvent(McRPGPlayer player, TooManyPlants tooManyPlants, Material plantType){
    super(tooManyPlants, player);
    this.plantType = plantType;
  }
}
