package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public class OnBlockBreakListener implements AbilityListener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        UUID uuid = blockBreakEvent.getPlayer().getUniqueId();
        activateAbilities(uuid, blockBreakEvent);
        readyAbilities(uuid, blockBreakEvent);
    }
}
