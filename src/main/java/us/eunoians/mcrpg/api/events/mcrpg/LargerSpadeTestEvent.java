package us.eunoians.mcrpg.api.events.mcrpg;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class LargerSpadeTestEvent extends FakeBlockBreakEvent {

  public LargerSpadeTestEvent(Player player, Block block){
    super(block, player);
  }

}
