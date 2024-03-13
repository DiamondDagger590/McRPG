package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * The components that activate {@link PoisonedBleed}
 */
public class PoisonedBleedComponents {

    public static final BleedPlusActivateComponent BLEED_PLUS_ACTIVATE_COMPONENT = new BleedPlusActivateComponent();

    private static class BleedPlusActivateComponent implements EventActivatableComponent {
        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return event instanceof BleedActivateEvent bleedActivateEvent && !bleedActivateEvent.isCancelled();
        }
    }
}
