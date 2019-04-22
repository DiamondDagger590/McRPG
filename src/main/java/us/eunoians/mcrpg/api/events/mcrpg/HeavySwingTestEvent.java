package us.eunoians.mcrpg.api.events.mcrpg;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class HeavySwingTestEvent extends BlockBreakEvent {

  public HeavySwingTestEvent(Player player, Block block){
    super(block, player);
  }
}
