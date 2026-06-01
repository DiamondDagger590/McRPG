package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link EntityDamageEvent} for damage taken objectives.
 */
public class DamageTakenQuestContext extends QuestObjectiveProgressContext {

    private final EntityDamageEvent damageEvent;

    public DamageTakenQuestContext(@NotNull EntityDamageEvent damageEvent) {
        this.damageEvent = damageEvent;
    }

    /**
     * Gets the underlying damage event.
     *
     * @return the entity damage event
     */
    @NotNull
    public EntityDamageEvent getDamageEvent() {
        return damageEvent;
    }
}
