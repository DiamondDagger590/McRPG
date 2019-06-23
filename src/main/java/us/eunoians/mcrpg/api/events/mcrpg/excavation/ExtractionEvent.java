package us.eunoians.mcrpg.api.events.mcrpg.excavation;

import lombok.Getter;
import org.bukkit.block.Block;
import us.eunoians.mcrpg.abilities.excavation.Extraction;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ExtractionEvent extends AbilityActivateEvent {

  @Getter
  private Block block;

  public ExtractionEvent(McRPGPlayer player, Extraction extraction, Block block) {
    super(extraction, player, AbilityEventType.RECREATIONAL);
    this.block = block;
  }
}
