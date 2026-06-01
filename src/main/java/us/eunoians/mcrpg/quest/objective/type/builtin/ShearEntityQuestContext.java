package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerShearEntityEvent} for shear entity objectives.
 * <p>
 * Pre-wraps the sheared entity in a {@link CustomEntityWrapper} for efficient matching.
 */
public class ShearEntityQuestContext extends QuestObjectiveProgressContext {

    private final PlayerShearEntityEvent shearEvent;
    private final CustomEntityWrapper entityWrapper;

    public ShearEntityQuestContext(@NotNull PlayerShearEntityEvent shearEvent) {
        this.shearEvent = shearEvent;
        this.entityWrapper = new CustomEntityWrapper(shearEvent.getEntity());
    }

    /**
     * Gets the underlying shear event.
     *
     * @return the player shear entity event
     */
    @NotNull
    public PlayerShearEntityEvent getShearEvent() {
        return shearEvent;
    }

    /**
     * Gets the McCore entity wrapper for the sheared entity.
     *
     * @return the custom entity wrapper
     */
    @NotNull
    public CustomEntityWrapper getEntityWrapper() {
        return entityWrapper;
    }
}
