package us.eunoians.mcrpg.api.event;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * This event relates to any event that handles some sort of {@link Ability} interaction.
 *
 * @author DiamondDagger590
 */
public abstract class AbilityEvent extends McRPGEvent{

    private final @NotNull AbilityHolder abilityHolder;
    private final @NotNull Ability ability;

    public AbilityEvent(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability){
        this.abilityHolder = abilityHolder;
        this.ability = ability;
    }

    /**
     * The {@link AbilityHolder} that activated this event
     *
     * @return The {@link AbilityHolder} that activated this event
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @NotNull
    public Ability getAbility() {
        return ability;
    }
}
