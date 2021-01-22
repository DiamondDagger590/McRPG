package us.eunoians.mcrpg.api.event.swords;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.bleedplus.BleedPlus;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.AbilityActivateEvent;

/**
 * This event is called whenever {@link us.eunoians.mcrpg.ability.impl.swords.bleedplus.BleedPlus} activates.
 *
 * @author DiamondDagger590
 */
public class BleedPlusActivateEvent extends AbilityActivateEvent {

    private int damagePerCycle;

    @NotNull
    private final BleedActivateEvent bleedActivateEvent;

    /**
     * @param abilityHolder The {@link AbilityHolder} that is activating the event
     * @param bleedPlus     The {@link Ability} being activated
     */
    public BleedPlusActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull BleedPlus bleedPlus, int damagePerCycle,
                                  @NotNull BleedActivateEvent bleedActivateEvent) {
        super(abilityHolder, bleedPlus, AbilityEventType.COMBAT);
        this.damagePerCycle = damagePerCycle;
        this.bleedActivateEvent = bleedActivateEvent;
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    public @NotNull BleedPlus getAbility() {
        return (BleedPlus) super.getAbility();
    }

    /**
     * Gets the {@link BleedActivateEvent} that caused this to activate
     *
     * @return The {@link BleedActivateEvent} that caused this to activate
     */
    @NotNull
    public BleedActivateEvent getBleedActivateEvent() {
        return bleedActivateEvent;
    }

    /**
     * Gets the amount of damage to be dealt each cycle of bleed
     *
     * @return A positive zero inclusive number representing the amount of damage taken each bleed cycle
     */
    public int getDamagePerCycle() {
        return damagePerCycle;
    }

    /**
     * Sets the amount of damage to be dealt each cycle of bleed
     *
     * @param damagePerCycle The amount of damage to be dealt each cycle of bleed. Needs to be a
     *                       positive zero inclusive int
     */
    public void setDamagePerCycle(int damagePerCycle) {
        this.damagePerCycle = Math.max(0, damagePerCycle);
    }
}
