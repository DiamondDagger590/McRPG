package us.eunoians.mcrpg.api.events.mcrpg.woodcutting;

import lombok.Getter;
import org.bukkit.block.Block;
import us.eunoians.mcrpg.abilities.woodcutting.HeavySwing;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

import java.util.ArrayList;

public class HeavySwingEvent extends AbilityActivateEvent {

  @Getter
  private ArrayList<Block> blocks;

  public HeavySwingEvent(McRPGPlayer player, HeavySwing heavySwing, ArrayList<Block> blocks){
    super(heavySwing, player, AbilityEventType.RECREATIONAL);
    this.blocks = blocks;
  }

}
