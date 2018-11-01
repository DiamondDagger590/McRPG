package us.eunoians.mcmmox.api.events.mcmmo;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class BlastTestEvent extends BlockBreakEvent {

  public BlastTestEvent(Block block, Player player){
    super(block, player);
  }
}
