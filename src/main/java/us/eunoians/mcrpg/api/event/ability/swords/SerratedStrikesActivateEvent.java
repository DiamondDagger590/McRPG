package us.eunoians.mcrpg.api.event.ability.swords;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.serratedstrikes.SerratedStrikes;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.CooldownableAbilityActivateEvent;

/**
 * This event is called whenever {@link SerratedStrikes} activates
 *
 * @author DiamondDagger590
 */
public class SerratedStrikesActivateEvent extends CooldownableAbilityActivateEvent {

    private double bleedModifyChance;
    private int durationInSeconds;

    /**
     * @param abilityHolder   The {@link AbilityHolder} that is activating the event
     * @param serratedStrikes The {@link SerratedStrikes} being activated
     */
    public SerratedStrikesActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull SerratedStrikes serratedStrikes, int cooldown, double bleedModifyChance, int durationInSeconds) {
        super(abilityHolder, serratedStrikes, AbilityEventType.COMBAT, cooldown);
        this.bleedModifyChance = Math.max(0, bleedModifyChance);
        this.durationInSeconds = Math.max(1, durationInSeconds);
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    public @NotNull SerratedStrikes getAbility() {
        return (SerratedStrikes) super.getAbility();
    }

    /**
     * Gets the additional rate at which {@link us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed} activates
     *
     * @return The positive zero inclusive additional rate at which {@link us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed} should activate
     */
    public double getBleedModifyChance() {
        return bleedModifyChance;
    }

    /**
     * Sets the additional rate at which {@link us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed} should activate for the duration of the ability
     *
     * @param bleedModifyChance A positive zero inclusive additional rate at which {@link us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed}
     *                          should activate
     */
    public void setBleedModifyChance(double bleedModifyChance) {
        this.bleedModifyChance = Math.max(0, bleedModifyChance);
    }

    /**
     * Gets the amount of time in seconds that this ability should be active for
     *
     * @return A positive zero exclusive amount of time in seconds that this ability should be active for
     */
    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    /**
     * Sets the amount of time in seconds that this ability should be active for
     *
     * @param durationInSeconds A positive zero exclusive amount of time in seconds that this ability should be active for
     */
    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = Math.max(1, durationInSeconds);
    }
}
