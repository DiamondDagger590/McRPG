package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link EntityDamageByEntityEvent} for deal damage objectives.
 * <p>
 * Pre-wraps the damaged entity in a {@link CustomEntityWrapper} for efficient matching.
 */
public class DealDamageQuestContext extends QuestObjectiveProgressContext {

    private final EntityDamageByEntityEvent damageEvent;
    private final CustomEntityWrapper targetWrapper;

    public DealDamageQuestContext(@NotNull EntityDamageByEntityEvent damageEvent) {
        this.damageEvent = damageEvent;
        this.targetWrapper = new CustomEntityWrapper(damageEvent.getEntity());
    }

    /**
     * Gets the underlying damage event.
     *
     * @return the entity damage by entity event
     */
    @NotNull
    public EntityDamageByEntityEvent getDamageEvent() {
        return damageEvent;
    }

    /**
     * Gets the McCore entity wrapper for the damaged entity.
     *
     * @return the custom entity wrapper for the target
     */
    @NotNull
    public CustomEntityWrapper getTargetWrapper() {
        return targetWrapper;
    }
}
