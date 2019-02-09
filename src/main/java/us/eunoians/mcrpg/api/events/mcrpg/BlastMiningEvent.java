package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import us.eunoians.mcrpg.abilities.mining.BlastMining;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

import java.util.ArrayList;

public class BlastMiningEvent extends AbilityActivateEvent {

  @Getter
  private ArrayList<Block> blocks;

  @Getter
  @Setter
  private int cooldown;

  public BlastMiningEvent(McRPGPlayer player, BlastMining blastMining, ArrayList<Block> blocks, int cooldown){
    super(blastMining, player, AbilityEventType.RECREATIONAL);
    this.blocks = blocks;
    this.cooldown = cooldown;
  }
}
