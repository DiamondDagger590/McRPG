package us.eunoians.mcrpg.ability.component.cancel;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.component.AbilityComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This ability component determines if an event should be cancelled when the ability activates.
 * Implementations can be registered in {@link BaseAbility#addCancellingComponent(EventCancellingComponent, Class, int)}.
 */
public interface EventCancellingComponent extends AbilityComponent {

    /**
     * Checks to see if this component should cancel the provided {@link Event} for
     * the given {@link AbilityHolder}.
     *
     * @param abilityHolder The {@link AbilityHolder} to check cancellation for
     * @param event         The {@link Event} to check cancellation against
     * @return {@code true} if this ability component should cause the event to be cancelled
     */
    boolean shouldCancel(@NotNull AbilityHolder abilityHolder, @NotNull Event event);
}
