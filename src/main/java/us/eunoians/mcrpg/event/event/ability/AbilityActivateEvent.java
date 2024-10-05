package us.eunoians.mcrpg.event.event.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is called whenever an {@link Ability} activates
 */
public abstract class AbilityActivateEvent extends AbilityEvent {

    private final AbilityHolder abilityHolder;

    public AbilityActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability) {
        super(ability);
        this.abilityHolder = abilityHolder;
    }

    /**
     * Gets the {@link AbilityHolder} that activated the {@link #getAbility() Ability}
     *
     * @return The {@link AbilityHolder} that activated the {@link #getAbility() Ability}
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }
}
