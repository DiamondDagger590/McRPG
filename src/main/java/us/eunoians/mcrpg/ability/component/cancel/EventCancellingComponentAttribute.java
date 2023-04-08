package us.eunoians.mcrpg.ability.component.cancel;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public record EventCancellingComponentAttribute(@NotNull EventCancellingComponent abilityComponent,
                                                @NotNull Class<? extends Event> clazz,
                                                int priority) {
}
