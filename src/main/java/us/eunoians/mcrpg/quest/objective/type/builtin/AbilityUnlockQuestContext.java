package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link AbilityUnlockEvent}. Carries the ability that was
 * unlocked and the holder who unlocked it.
 */
public class AbilityUnlockQuestContext extends QuestObjectiveProgressContext {

    private final AbilityUnlockEvent abilityUnlockEvent;

    /**
     * Creates a context from the given ability unlock event.
     *
     * @param abilityUnlockEvent the event that triggered this context
     */
    public AbilityUnlockQuestContext(@NotNull AbilityUnlockEvent abilityUnlockEvent) {
        this.abilityUnlockEvent = abilityUnlockEvent;
    }

    /**
     * Gets the ability that was unlocked.
     *
     * @return the unlocked ability
     */
    @NotNull
    public UnlockableAbility getAbility() {
        return abilityUnlockEvent.getAbility();
    }

    /**
     * Gets the ability holder who unlocked the ability.
     *
     * @return the ability holder
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityUnlockEvent.getAbilityHolder();
    }
}
