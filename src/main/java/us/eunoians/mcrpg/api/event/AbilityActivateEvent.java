package us.eunoians.mcrpg.api.event;

import org.bukkit.event.Cancellable;
import us.eunoians.mcrpg.abilities.Ability;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This event is called when a {@link McRPGPlayer} activates an {@link Ability}. This will always be called with a custom implementation
 * for specific abilities but this defines the generic behaviour that all ability activation events will have.
 * <p>
 * This event is called BEFORE the actual ability is activated and can be cancelled. Any modified values will be used provided
 * the event actually is uncancelled by the time it gets returned.
 *
 * @author DiamondDagger590
 */
public abstract class AbilityActivateEvent extends McRPGEvent implements Cancellable {

    private boolean cancelled;

    private final McRPGPlayer mcRPGPlayer;
    private final Ability ability;
    private final AbilityEventType abilityEventType;

    /**
     * @param mcRPGPlayer      The {@link McRPGPlayer} that is activating the event
     * @param ability          The {@link Ability} being activated
     * @param abilityEventType The {@link AbilityEventType} that specifies the generic reason the event was called
     */
    public AbilityActivateEvent(McRPGPlayer mcRPGPlayer, Ability ability, AbilityEventType abilityEventType) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.ability = ability;
        this.abilityEventType = abilityEventType;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * The {@link McRPGPlayer} that activated this event
     *
     * @return The {@link McRPGPlayer} that activated this event
     */
    public McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    public Ability getAbility() {
        return ability;
    }

    /**
     * Gets the {@link AbilityEventType} that holds some basic information about why the event was activated
     *
     * @return The {@link AbilityEventType} that holds some basic information about why the event was activated
     */
    public AbilityEventType getAbilityEventType() {
        return abilityEventType;
    }

    /**
     * This enum specifies some generic reasons as to why the event is being called
     */
    public enum AbilityEventType {

        /**
         * The event was called for more passive purposes
         */
        RECREATIONAL,
        /**
         * The event was called for combat related purposes
         */
        COMBAT,
        /**
         * The event was called for random purposes
         */
        OTHER
    }
}
