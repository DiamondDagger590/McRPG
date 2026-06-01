package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.event.entity.EntityTameEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link EntityTameEvent} for tame animal objectives.
 * <p>
 * Pre-wraps the tamed entity in a {@link CustomEntityWrapper} for efficient matching.
 */
public class TameAnimalQuestContext extends QuestObjectiveProgressContext {

    private final EntityTameEvent tameEvent;
    private final CustomEntityWrapper entityWrapper;

    public TameAnimalQuestContext(@NotNull EntityTameEvent tameEvent) {
        this.tameEvent = tameEvent;
        this.entityWrapper = new CustomEntityWrapper(tameEvent.getEntity());
    }

    /**
     * Gets the underlying tame event.
     *
     * @return the entity tame event
     */
    @NotNull
    public EntityTameEvent getTameEvent() {
        return tameEvent;
    }

    /**
     * Gets the McCore entity wrapper for the tamed entity.
     *
     * @return the custom entity wrapper
     */
    @NotNull
    public CustomEntityWrapper getEntityWrapper() {
        return entityWrapper;
    }
}
