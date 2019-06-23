package us.eunoians.mcrpg.api.events.mcrpg;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class PansShrineTestEvent extends BlockBreakEvent {

  public PansShrineTestEvent(Player player, Block block) { super(block, player);
  }
}

