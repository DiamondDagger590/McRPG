package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link ProjectileHitEvent} for projectile hit objectives.
 */
public class ProjectileHitQuestContext extends QuestObjectiveProgressContext {

    private final ProjectileHitEvent projectileHitEvent;

    public ProjectileHitQuestContext(@NotNull ProjectileHitEvent projectileHitEvent) {
        this.projectileHitEvent = projectileHitEvent;
    }

    /**
     * Gets the underlying projectile hit event.
     *
     * @return the projectile hit event
     */
    @NotNull
    public ProjectileHitEvent getProjectileHitEvent() {
        return projectileHitEvent;
    }
}
