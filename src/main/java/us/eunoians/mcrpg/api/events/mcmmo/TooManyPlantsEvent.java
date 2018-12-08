package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.herbalism.TooManyPlants;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class TooManyPlantsEvent extends AbilityActivateEvent {

  @Getter
  private Material plantType;

  public TooManyPlantsEvent(McMMOPlayer player, TooManyPlants tooManyPlants, Material plantType){
    super(tooManyPlants, player);
    this.plantType = plantType;
  }
}
