package us.eunoians.mcrpg.ability.component.activatable;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.ability.component.AbilityComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This ability component allows for ability activation by providing custom implementation
 * and registering them in {@link BaseAbility#addActivatableComponent(EventActivatableComponent, Class, int)}
 */
public interface EventActivatableComponent extends AbilityComponent {

    /**
     * Checks to see if this component should activate for the given {@link AbilityHolder} for
     * the provided {@link Event}.
     *
     * @param abilityHolder The {@link AbilityHolder} to check activation for
     * @param event         The {@link Event} to check activation against
     * @return {@code true} if this ability component should activate
     */
    public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event);
}
