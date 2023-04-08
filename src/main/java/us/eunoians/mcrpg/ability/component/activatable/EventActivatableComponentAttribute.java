package us.eunoians.mcrpg.ability.component.activatable;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * This record stores an {@link EventActivatableComponent} along with the {@link Event} that
 * can activate it and the priority in which it should be checked.
 * @param abilityComponent The {@link EventActivatableComponent} that can be activated
 * @param clazz The {@link Class} of the {@link Event} that can activate the component
 * @param priority The priority in which the component should be checked.
 */
public record EventActivatableComponentAttribute(@NotNull EventActivatableComponent abilityComponent,
                                                 @NotNull Class<? extends Event> clazz,
                                                 int priority) {
}
