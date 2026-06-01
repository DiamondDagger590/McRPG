package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link ProjectileLaunchEvent} for launch projectile objectives.
 */
public class LaunchProjectileQuestContext extends QuestObjectiveProgressContext {

    private final ProjectileLaunchEvent launchEvent;

    public LaunchProjectileQuestContext(@NotNull ProjectileLaunchEvent launchEvent) {
        this.launchEvent = launchEvent;
    }

    /**
     * Gets the underlying projectile launch event.
     *
     * @return the projectile launch event
     */
    @NotNull
    public ProjectileLaunchEvent getLaunchEvent() {
        return launchEvent;
    }
}
