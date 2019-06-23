package us.eunoians.mcrpg.api.events.mcrpg;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class LargerSpadeTestEvent extends BlockBreakEvent {

  public LargerSpadeTestEvent(Player player, Block block){
    super(block, player);
  }

}
