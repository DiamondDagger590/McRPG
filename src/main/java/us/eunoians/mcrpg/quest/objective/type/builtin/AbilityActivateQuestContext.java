package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link AbilityActivateEvent}. Carries the ability that was
 * activated and the holder who activated it.
 */
public class AbilityActivateQuestContext extends QuestObjectiveProgressContext {

    private final AbilityActivateEvent abilityActivateEvent;

    /**
     * Creates a context from the given ability activate event.
     *
     * @param abilityActivateEvent the event that triggered this context
     */
    public AbilityActivateQuestContext(@NotNull AbilityActivateEvent abilityActivateEvent) {
        this.abilityActivateEvent = abilityActivateEvent;
    }

    /**
     * Gets the ability that was activated.
     *
     * @return the activated ability
     */
    @NotNull
    public Ability getAbility() {
        return abilityActivateEvent.getAbility();
    }

    /**
     * Gets the ability holder who activated the ability.
     *
     * @return the ability holder
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityActivateEvent.getAbilityHolder();
    }
}
