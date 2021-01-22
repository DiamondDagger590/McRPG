package us.eunoians.mcrpg.api.event.swords;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.vampire.Vampire;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.AbilityActivateEvent;

/**
 * This event is called whenever {@link Vampire} activates
 *
 * @author DiamondDagger590
 */
public class VampireActivateEvent extends AbilityActivateEvent {

    private int amountToHeal;
    @NotNull
    private BleedActivateEvent bleedActivateEvent;

    /**
     * @param abilityHolder    The {@link AbilityHolder} that is activating the event
     * @param vampire          The {@link Ability} being activated
     */
    public VampireActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Vampire vampire, int amountToHeal, @NotNull BleedActivateEvent bleedActivateEvent) {
        super(abilityHolder, vampire, AbilityEventType.COMBAT);
        this.amountToHeal = amountToHeal;
        this.bleedActivateEvent = bleedActivateEvent;
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    public @NotNull Vampire getAbility() {
        return (Vampire) super.getAbility();
    }

    /**
     * Gets the amount of health to restore to the {@link BleedActivateEvent#getAbilityHolder()} each bleed cycle
     * @return The amount of health to restore to the {@link BleedActivateEvent#getAbilityHolder()} each bleed cycle
     */
    public int getAmountToHeal() {
        return amountToHeal;
    }

    /**
     * Sets the amount of health to restore to the {@link BleedActivateEvent#getAbilityHolder()} each bleed cycle
     * @param amountToHeal The amount of health to restore to the {@link BleedActivateEvent#getAbilityHolder()} each bleed cycle
     */
    public void setAmountToHeal(int amountToHeal) {
        this.amountToHeal = amountToHeal;
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
}
