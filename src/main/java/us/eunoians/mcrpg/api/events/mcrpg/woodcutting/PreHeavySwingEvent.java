package us.eunoians.mcrpg.api.events.mcrpg.woodcutting;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.woodcutting.HeavySwing;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class PreHeavySwingEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int range;

  @Getter
  private Material woodType;

  public PreHeavySwingEvent(McRPGPlayer player, HeavySwing heavySwing, int range, Material woodType){
    super(heavySwing, player, AbilityEventType.RECREATIONAL);
    this.range = range;
    this.woodType = woodType;
  }
}
