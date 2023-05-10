package us.eunoians.mcrpg.exception.skill;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;

/**
 * This exception is thrown whenever experience is attempted to be given to a {@link Skill} without
 * from an {@link Event} without that {@link Event} being registered.
 */
public class EventNotRegisteredForLevelingException extends RuntimeException {

    private final Event event;
    private final Skill skill;

    public EventNotRegisteredForLevelingException(@NotNull Event event, @NotNull Skill skill) {
        this.event = event;
        this.skill = skill;
    }

    /**
     * Gets the {@link Event} that was not registered.
     *
     * @return The {@link Event} that was not registered
     */
    @NotNull
    public Event getFailedEvent() {
        return event;
    }

    /**
     * Gets the {@link Skill} that the {@link Event} was trying to be registered to
     *
     * @return The {@link Skill} that the {@link Event} was trying to be registered to
     */
    @NotNull
    public Skill getSkill() {
        return skill;
    }
}
