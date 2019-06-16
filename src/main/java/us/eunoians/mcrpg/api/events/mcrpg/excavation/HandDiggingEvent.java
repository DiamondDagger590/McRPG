package us.eunoians.mcrpg.api.events.mcrpg.excavation;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.excavation.HandDigging;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

import java.util.Set;

public class HandDiggingEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int duration;

  @Getter
  @Setter
  private int cooldown;

  @Getter
  private Set<Material> breakableBlocks;

  public HandDiggingEvent(McRPGPlayer player, HandDigging handDigging, int duration, int cooldown, Set<Material> breakableBlocks) {
    super(handDigging, player, AbilityEventType.RECREATIONAL);
    this.duration = duration;
    this.cooldown = cooldown;
    this.breakableBlocks = breakableBlocks;
  }
}
