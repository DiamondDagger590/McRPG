package us.eunoians.mcrpg.ability.component.readyable;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public record EventReadyableComponentAttribute(@NotNull EventReadyableComponent abilityComponent,
                                               @NotNull Class<? extends Event> clazz,
                                               int priority) {
}
