package us.eunoians.mcrpg.api.event;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This event is called when a {@link McRPGPlayer} activates an {@link Ability}. This will always be called with a custom implementation
 * for specific abilities but this defines the generic behaviour that all ability activation events will have.
 * <p>
 * This event is called BEFORE the actual ability is activated and can be cancelled. Any modified values will be used provided
 * the event actually is uncancelled by the time it gets returned.
 * <p>
 * For {@link us.eunoians.mcrpg.ability.CooldownableAbility}s, {@link us.eunoians.mcrpg.ability.listener.CooldownableAbilityListener} automatically
 * listens to the child of this class, {@link CooldownableAbilityActivateEvent}, and handles all needed integration provided this event is called
 * before activation and adheres to Bukkit/Spigot event call standards.
 * <p>
 * {@link us.eunoians.mcrpg.ability.listener.CooldownableAbilityListener} listens to the child of this class, {@link CooldownableAbilityActivateEvent},
 * on {@link org.bukkit.event.EventPriority#LOWEST} which means that no other plugin should listen to this event
 * on the same priority because there is a chance that you will get an uncancelled event when it should be cancelled due to your event handler being
 * called first.
 *
 * @author DiamondDagger590
 */
public abstract class AbilityActivateEvent extends AbilityEvent implements Cancellable {

    private boolean cancelled;
    private final @NotNull AbilityEventType abilityEventType;

    /**
     * @param abilityHolder    The {@link AbilityHolder} that is activating the event
     * @param ability          The {@link Ability} being activated
     * @param abilityEventType The {@link AbilityEventType} that specifies the generic reason the event was called
     */
    public AbilityActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability, @NotNull AbilityEventType abilityEventType) {
        super(abilityHolder, ability);
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
     * Gets the {@link AbilityEventType} that holds some basic information about why the event was activated
     *
     * @return The {@link AbilityEventType} that holds some basic information about why the event was activated
     */
    @NotNull
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
