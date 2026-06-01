package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.ProjectileHitQuestContext;

/**
 * Listens for {@link ProjectileHitEvent} and drives quest objective progress for any
 * active projectile-hit objectives the shooting player is contributing to.
 * <p>
 * Only events where a projectile hit an entity (not a block) and the shooter is a
 * {@link Player} are processed.
 */
public class ProjectileHitQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public ProjectileHitQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a projectile hit event by progressing any matching quest objectives
     * for the player who shot the projectile, provided it hit an entity.
     *
     * @param event the projectile hit event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() == null) {
            return;
        }
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        progressQuests(questManager, player.getUniqueId(), new ProjectileHitQuestContext(event));
    }
}
