package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link EntityDeathEvent} for mob kill objectives.
 * <p>
 * Wraps the killed entity in a {@link CustomEntityWrapper} so that both vanilla
 * and custom entity types from McCore-integrated plugins are supported.
 */
public class MobKillQuestContext extends QuestObjectiveProgressContext {

    private final EntityDeathEvent deathEvent;
    private final CustomEntityWrapper entityWrapper;

    public MobKillQuestContext(@NotNull EntityDeathEvent deathEvent) {
        this.deathEvent = deathEvent;
        this.entityWrapper = new CustomEntityWrapper(deathEvent.getEntity());
    }

    /**
     * Gets the underlying death event.
     *
     * @return the entity death event
     */
    @NotNull
    public EntityDeathEvent getDeathEvent() {
        return deathEvent;
    }

    /**
     * Gets the McCore entity wrapper for the killed entity, supporting both vanilla
     * and custom entity identification.
     *
     * @return the custom entity wrapper
     */
    @NotNull
    public CustomEntityWrapper getEntityWrapper() {
        return entityWrapper;
    }
}
