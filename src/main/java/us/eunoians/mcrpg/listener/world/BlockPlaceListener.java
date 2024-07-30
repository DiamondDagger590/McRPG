package us.eunoians.mcrpg.listener.world;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import us.eunoians.mcrpg.world.WorldManager;

public class BlockPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        WorldManager.markBlockAsPlaced(event.getBlockPlaced());
    }
}
