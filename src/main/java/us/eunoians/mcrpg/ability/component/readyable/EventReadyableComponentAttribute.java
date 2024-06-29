package us.eunoians.mcrpg.ability.component.readyable;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * This record stores an {@link EventReadyableComponent} along with the {@link Event} that
 * can readied it and the priority in which it should be checked.
 *
 * @param abilityComponent The {@link EventReadyableComponent} that can be readied
 * @param clazz            The {@link Class} of the {@link Event} that can ready the component
 * @param priority         The priority in which the component should be checked.
 */
public record EventReadyableComponentAttribute(@NotNull EventReadyableComponent abilityComponent,
                                               @NotNull Class<? extends Event> clazz,
                                               int priority) {
}
