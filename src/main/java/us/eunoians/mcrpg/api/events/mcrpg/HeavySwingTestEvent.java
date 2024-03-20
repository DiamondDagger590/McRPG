package us.eunoians.mcrpg.api.events.mcrpg;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class HeavySwingTestEvent extends FakeBlockBreakEvent {

  public HeavySwingTestEvent(Player player, Block block){
    super(block, player);
  }
}
