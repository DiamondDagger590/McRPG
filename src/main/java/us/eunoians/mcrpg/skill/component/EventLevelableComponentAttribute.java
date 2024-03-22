package us.eunoians.mcrpg.skill.component;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * A record containing the information needed to store, sort, and use a {@link EventLevelableComponent}.
 *
 * @param levelableComponent The {@link EventLevelableComponent} to use
 * @param clazz              The {@link Event} that can activate this component
 * @param priority           The priority in which this component is processed (0 is the highest)
 */
public record EventLevelableComponentAttribute(@NotNull EventLevelableComponent levelableComponent,
                                               @NotNull Class<? extends Event> clazz,
                                               int priority) {
}
