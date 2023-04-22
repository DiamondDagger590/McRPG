package us.eunoians.mcrpg.exception.skill;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;

public class EventNotRegisteredForLeveling extends RuntimeException{

    private final Event event;
    private final Skill skill;

    public EventNotRegisteredForLeveling(@NotNull Event event, @NotNull Skill skill) {
        this.event = event;
        this.skill = skill;
    }

    @NotNull
    public Event getFailedEvent() {
        return event;
    }

    @NotNull
    public Skill getSkill() {
        return skill;
    }
}
