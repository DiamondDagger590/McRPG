package us.eunoians.mcrpg.exception.ability;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;

/**
 * This exception is fired whenever an {@link Event} is checked for readying,
 * but it isn't actually registered for readying.
 */
public class EventNotRegisteredForReadyingException extends RuntimeException {

    private final Event event;
    private final Ability ability;

    public EventNotRegisteredForReadyingException(@NotNull Event event, @NotNull Ability ability) {
        this.event = event;
        this.ability = ability;
    }

    /**
     * Gets the {@link Event} that was not registered.
     *
     * @return The {@link Event} that was not registered.
     */
    @NotNull
    public Event getFailedEvent() {
        return event;
    }

    /**
     * Gets the {@link Ability} that didn't have the {@link Event} registered
     * for readying.
     *
     * @return The {@link Ability} that didn't have the {@link Event} registered
     * for readying.
     */
    @NotNull
    public Ability getAbility() {
        return ability;
    }
}