package us.eunoians.mcrpg.api.event.ability;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.ReadyableAbility;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * This event is called whenever an {@link ReadyableAbility} is about to be set into a "ready" status.
 * <p>
 * This event is called after all other checks along with {@link ReadyableAbility#handleReadyAttempt(Event)}. If cancelled,
 * then {@link ReadyableAbility#startReady(int)} will not be called.
 *
 * @author DiamondDagger590
 */
public class AbilityReadyEvent extends AbilityEvent implements Cancellable {

    private int readySeconds;
    private boolean cancelled;

    public AbilityReadyEvent(@NotNull AbilityHolder abilityHolder, @NotNull ReadyableAbility ability, int readySeconds) {
        super(abilityHolder, ability);
        this.readySeconds = readySeconds;
    }

    /**
     * Gets the amount of seconds that the user should be put on ready for
     *
     * @return A positive zero exclusive amount of seconds that the user should be put on ready for
     */
    public int getReadySeconds() {
        return readySeconds;
    }

    /**
     * Sets the amount of seconds that the user should be put on ready for
     *
     * @param readySeconds A positive zero exclusive amount of seconds that the user should be put on ready status for
     */
    public void setReadySeconds(int readySeconds) {
        this.readySeconds = Math.max(1, readySeconds);
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    public @NotNull ReadyableAbility getAbility() {
        return (ReadyableAbility) super.getAbility();
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

}
