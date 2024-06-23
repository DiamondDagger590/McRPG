package us.eunoians.mcrpg.exception.ability;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.BaseAbility;

public class EventNotRegisteredForActivation extends RuntimeException {

    private final Event event;
    private final Ability ability;

    public EventNotRegisteredForActivation(@NotNull Event event, @NotNull Ability ability) {
        this.event = event;
        this.ability = ability;
    }

    @NotNull
    public Event getFailedEvent() {
        return event;
    }

    @NotNull
    public Ability getAbility() {
        return ability;
    }
}
