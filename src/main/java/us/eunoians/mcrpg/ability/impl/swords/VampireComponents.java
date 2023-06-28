package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * The components in charge of activating {@link Vampire}
 */
public class VampireComponents {

    public static final VampireActivateComponent VAMPIRE_ACTIVATE_COMPONENT = new VampireActivateComponent();

    private static class VampireActivateComponent implements EventActivatableComponent {
        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return event instanceof BleedActivateEvent bleedActivateEvent && !bleedActivateEvent.isCancelled()
                    && Bukkit.getEntity(abilityHolder.getUUID()) instanceof LivingEntity;
        }
    }
}
