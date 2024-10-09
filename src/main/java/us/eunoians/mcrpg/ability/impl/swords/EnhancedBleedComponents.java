package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * The components that activate {@link EnhancedBleed}
 */
public class EnhancedBleedComponents {

    public static final EnhancedBleedComponent ENHANCED_BLEED_ACTIVATE_COMPONENT = new EnhancedBleedComponent();

    private static class EnhancedBleedComponent implements EventActivatableComponent {
        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return event instanceof BleedActivateEvent bleedActivateEvent && !bleedActivateEvent.isCancelled();
        }
    }
}
