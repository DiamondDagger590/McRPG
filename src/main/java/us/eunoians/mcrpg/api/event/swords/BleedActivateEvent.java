package us.eunoians.mcrpg.api.event.swords;

import org.bukkit.entity.LivingEntity;
import us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.AbilityActivateEvent;

/**
 * This {@link us.eunoians.mcrpg.ability.Ability} is called when a {@link org.bukkit.entity.LivingEntity}
 * attacks another {@link org.bukkit.entity.LivingEntity} with a Sword.
 * <p>
 * This allows other abilities to modify how {@link Bleed} functions.
 *
 * @author DiamondDagger590
 */
public class BleedActivateEvent extends AbilityActivateEvent {

    /**
     * Gets the {@link LivingEntity} being affected by {@link Bleed}
     */
    private final LivingEntity target;

    /**
     * How many ticks it should take between each Bleed cycle
     */
    private int cycleFrequencyInTicks;

    /**
     * The amount of damage to be dealt on each Bleed cycle
     */
    private int damagePerCycle;

    /**
     * The amount of cycles that the Bleed can inflict
     */
    private int amountOfCycles;

    /**
     * If the Bleed effect should restore health on each cycle
     */
    private boolean restoreHealth;

    /**
     * The amount of health to restore on each Bleed cycle if {@link #restoreHealth} is true
     */
    private int healthToRestore;

    public BleedActivateEvent(AbilityHolder abilityHolder, Bleed bleed, LivingEntity target, int cycleFrequencyInTicks, int damagePerCycle, int amountOfCycles, boolean restoreHealth,
                              int healthToRestore) {
        super(abilityHolder, bleed, AbilityEventType.COMBAT);
        this.target = target;
        this.cycleFrequencyInTicks = cycleFrequencyInTicks;
        this.damagePerCycle = damagePerCycle;
        this.amountOfCycles = amountOfCycles;
        this.restoreHealth = restoreHealth;
        this.healthToRestore = healthToRestore;
    }

    /**
     * Gets the amount of health to be restored from each bleed cycle
     *
     * @return A positive zero inclusive integer of health to be restored per bleed cycle
     */
    public int getHealthToRestore() {
        return healthToRestore;
    }

    /**
     * Sets the amount of health to be restored from each bleed cycle
     *
     * @param healthToRestore A positive zero inclusive integer representing the health to be restored per bleed cycle
     */
    public void setHealthToRestore(int healthToRestore) {
        this.healthToRestore = Math.max(0, healthToRestore);
    }

    /**
     * Checks to see if the bleed cycles should restore health to the activator.
     * <p>
     * Health healed per cycle comes from {@link #getHealthToRestore()}
     *
     * @return {@code true} if the activator should be healed
     */
    public boolean isRestoreHealth() {
        return restoreHealth;
    }

    /**
     * Sets if the bleed cycles should restore health to the activator.
     * <p>
     * Health healed per cycle comes from {@link #getHealthToRestore()}
     *
     * @param restoreHealth
     */
    public void setRestoreHealth(boolean restoreHealth) {
        this.restoreHealth = restoreHealth;
    }

    /**
     * Gets the amount of times that the bleed task will run and cause damage to the target
     *
     * @return A positive zero exclusive number of cycles for the bleed task to run for
     */
    public int getAmountOfCycles() {
        return amountOfCycles;
    }

    /**
     * Sets the amount of cycles that the bleed tasks should run for
     *
     * @param amountOfCycles A positive zero exclusive number of cycles for the bleed task to run for
     */
    public void setAmountOfCycles(int amountOfCycles) {
        this.amountOfCycles = Math.max(1, amountOfCycles);
    }

    /**
     * Gets how often the bleed task should be ran in ticks. (Not seconds. Each second is 20 ticks)
     *
     * @return A positive zero exclusive number representing the amount of ticks between each cycle of the bleed task
     */
    public int getCycleFrequencyInTicks() {
        return cycleFrequencyInTicks;
    }

    /**
     * Sets how often the bleed task should be ran in ticks. (Not seconds. Each second is 20 ticks)
     *
     * @param cycleFrequencyInTicks A positive zero exclusive number representing the amount of ticks between each cycle of the bleed task
     */
    public void setCycleFrequencyInTicks(int cycleFrequencyInTicks) {
        this.cycleFrequencyInTicks = Math.max(1, cycleFrequencyInTicks);
    }

    /**
     * Gets the amount of damage to be dealt to the {@link #getTarget()} each cycle
     *
     * @return A positive zero inclusive amount of damage to be dealt to the {@link #getTarget()} each cycle
     */
    public int getDamagePerCycle() {
        return damagePerCycle;
    }

    /**
     * Sets the amount of damage to be dealt to the {@link #getTarget()} each cycle
     *
     * @param damagePerCycle A positive zero inclusive amount of damage to be dealt to the {@link #getTarget()} each cycle
     */
    public void setDamagePerCycle(int damagePerCycle) {
        this.damagePerCycle = Math.max(0, damagePerCycle);
    }

    /**
     * Gets the {@link LivingEntity} that is being affected by the bleed task
     *
     * @return The {@link LivingEntity} that is being affected by the bleed task
     */
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public Bleed getAbility() {
        return (Bleed) super.getAbility();
    }
}
