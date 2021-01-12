package us.eunoians.mcrpg.api.event.swords;

import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed;
import us.eunoians.mcrpg.api.event.AbilityActivateEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This {@link us.eunoians.mcrpg.ability.Ability} is called when a {@link org.bukkit.entity.LivingEntity}
 * attacks another {@link org.bukkit.entity.LivingEntity} with a Sword.
 *
 * This allows other abilities to modify how {@link Bleed} functions.
 *
 * @author DiamondDagger590
 */
public class BleedActivateEvent extends AbilityActivateEvent {

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

    /**
     * How long should the target be immune from bleeding for after it wears off
     */
    private long bleedImmunityDuration;

    /**
     * @param mcRPGPlayer      The {@link McRPGPlayer} that is activating the event
     * @param ability          The {@link Ability} being activated
     */
    public BleedActivateEvent(McRPGPlayer mcRPGPlayer, Ability ability) {
        super(mcRPGPlayer, ability, AbilityEventType.COMBAT);
    }
}
