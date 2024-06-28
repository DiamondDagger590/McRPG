package us.eunoians.mcrpg.ability.component.readyable;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.AbilityComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public interface EventReadyableComponent extends AbilityComponent {

    /**
     * Checks to see if this component should ready for the given {@link AbilityHolder} for
     * the provided {@link Event}.
     *
     * @param abilityHolder The {@link AbilityHolder} to check ready for
     * @param event         The {@link Event} to check ready against
     * @return {@code true} if this ready component should activate
     */
    public boolean shouldReady(@NotNull AbilityHolder abilityHolder, @NotNull Event event);
}
