package us.eunoians.mcrpg.api.event.ability.swords.ragespike;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.ragespike.RageSpike;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.AbilityEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This event is a stub that can be implemented by any event that deals with {@link RageSpike}
 *
 * @author DiamondDagger590
 */
public abstract class RageSpikeNonActivationEvent extends AbilityEvent {

    public RageSpikeNonActivationEvent(@NotNull AbilityHolder abilityHolder, @NotNull RageSpike rageSpike) {
        super(abilityHolder, rageSpike);
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    public @NotNull RageSpike getAbility() {
        return (RageSpike) super.getAbility();
    }

    /**
     * The {@link AbilityHolder} that activated this event
     *
     * @return The {@link AbilityHolder} that activated this event
     */
    @Override
    public @NotNull McRPGPlayer getAbilityHolder() {
        return (McRPGPlayer) super.getAbilityHolder();
    }
}
