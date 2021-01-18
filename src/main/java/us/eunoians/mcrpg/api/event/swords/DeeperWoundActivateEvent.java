package us.eunoians.mcrpg.api.event.swords;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.swords.deeperwound.DeeperWound;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.AbilityActivateEvent;

/**
 * This event is called when {@link DeeperWound} is activated
 *
 * @author DiamondDagger590
 */
public class DeeperWoundActivateEvent extends AbilityActivateEvent {

    private int amountOfCyclesToSet;

    @NotNull
    private final BleedActivateEvent bleedActivateEvent;

    /**
     * @param abilityHolder The {@link AbilityHolder} that is activating the event
     * @param deeperWound   The {@link DeeperWound} being activated
     */
    public DeeperWoundActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull DeeperWound deeperWound,
                                    int amountOfCyclesToSet, @NotNull BleedActivateEvent bleedActivateEvent) {
        super(abilityHolder, deeperWound, AbilityEventType.COMBAT);
        this.amountOfCyclesToSet = amountOfCyclesToSet;
        this.bleedActivateEvent = bleedActivateEvent;
    }

    /**
     * Gets the {@link BleedActivateEvent} that is being modified
     *
     * @return The {@link BleedActivateEvent} that is being modified
     */
    @NotNull
    public BleedActivateEvent getBleedActivateEvent() {
        return bleedActivateEvent;
    }

    /**
     * Gets the new amount of cycles for the bleed effect to run for
     *
     * @return A positive zero exclusive number of how many cycles the bleed effect should run for
     */
    public int getAmountOfCyclesToSet() {
        return amountOfCyclesToSet;
    }

    /**
     * Sets the new amount of cycles for the bleed effect to run for
     *
     * @param amountOfCyclesToSet The new amount of cycles for the bleed effect to run for. This
     *                            needs to be a positive zero exclusive number.
     */
    public void setAmountOfCyclesToSet(int amountOfCyclesToSet) {
        this.amountOfCyclesToSet = Math.max(1, amountOfCyclesToSet);
    }
}
