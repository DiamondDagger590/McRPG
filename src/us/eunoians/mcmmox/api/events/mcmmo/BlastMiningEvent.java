package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import us.eunoians.mcmmox.abilities.mining.BlastMining;
import us.eunoians.mcmmox.players.McMMOPlayer;

import java.util.ArrayList;

public class BlastMiningEvent extends AbilityActivateEvent {

  @Getter
  private ArrayList<Block> blocks;

  @Getter
  @Setter
  private int cooldown;

  public BlastMiningEvent(McMMOPlayer player, BlastMining blastMining, ArrayList<Block> blocks, int cooldown){
    super(blastMining, player);
    this.blocks = blocks;
    this.cooldown = cooldown;
  }
}
