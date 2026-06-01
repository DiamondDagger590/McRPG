package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.LaunchProjectileQuestContext;

/**
 * Listens for {@link ProjectileLaunchEvent} and drives quest objective progress for any
 * active projectile-launch objectives the launching player is contributing to.
 * <p>
 * Only events where the projectile's shooter is a {@link Player} are processed.
 */
public class LaunchProjectileQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public LaunchProjectileQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a projectile launch event by progressing any matching quest objectives
     * for the player who launched the projectile.
     *
     * @param event the projectile launch event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLaunchProjectile(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        progressQuests(questManager, player.getUniqueId(), new LaunchProjectileQuestContext(event));
    }
}
