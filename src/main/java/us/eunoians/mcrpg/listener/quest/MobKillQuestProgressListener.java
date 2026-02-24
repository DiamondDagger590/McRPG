package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import us.eunoians.mcrpg.quest.objective.type.builtin.MobKillQuestContext;

/**
 * Listens for {@link EntityDeathEvent} and drives quest objective progress for any
 * active mob-kill objectives when the killer is a player.
 */
public class MobKillQuestProgressListener implements QuestProgressListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player killer) {
            progressQuests(killer.getUniqueId(), new MobKillQuestContext(event));
        }
    }
}
