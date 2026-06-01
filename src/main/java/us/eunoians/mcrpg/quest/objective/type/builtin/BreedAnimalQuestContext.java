package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.event.entity.EntityBreedEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link EntityBreedEvent} for breed animal objectives.
 * <p>
 * Pre-wraps the child entity in a {@link CustomEntityWrapper} for efficient matching.
 */
public class BreedAnimalQuestContext extends QuestObjectiveProgressContext {

    private final EntityBreedEvent breedEvent;
    private final CustomEntityWrapper childWrapper;

    public BreedAnimalQuestContext(@NotNull EntityBreedEvent breedEvent) {
        this.breedEvent = breedEvent;
        this.childWrapper = new CustomEntityWrapper(breedEvent.getEntity());
    }

    /**
     * Gets the underlying breed event.
     *
     * @return the entity breed event
     */
    @NotNull
    public EntityBreedEvent getBreedEvent() {
        return breedEvent;
    }

    /**
     * Gets the McCore entity wrapper for the bred child entity.
     *
     * @return the custom entity wrapper for the child
     */
    @NotNull
    public CustomEntityWrapper getChildWrapper() {
        return childWrapper;
    }
}
