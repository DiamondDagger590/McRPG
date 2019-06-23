package us.eunoians.mcrpg.api.events.mcrpg.excavation;

import lombok.Getter;
import org.bukkit.block.Block;
import us.eunoians.mcrpg.abilities.excavation.LargerSpade;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

import java.util.ArrayList;

public class LargerSpadeEvent extends AbilityActivateEvent {

  @Getter
  private ArrayList<Block> blocks;

  public LargerSpadeEvent(McRPGPlayer player, LargerSpade largerSpade, ArrayList<Block> blocks){
    super(largerSpade, player, AbilityEventType.RECREATIONAL);
    this.blocks = blocks;
  }

}
