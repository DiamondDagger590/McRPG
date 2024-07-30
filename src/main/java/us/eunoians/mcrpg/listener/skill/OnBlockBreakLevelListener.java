package us.eunoians.mcrpg.listener.skill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class OnBlockBreakLevelListener implements SkillListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        levelSkill(event.getPlayer().getUniqueId(), event);
    }
}
