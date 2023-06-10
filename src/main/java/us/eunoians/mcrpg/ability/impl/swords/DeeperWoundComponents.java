package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public class DeeperWoundComponents {

    public static final DeeperWoundActivateComponent DEEPER_WOUND_ACTIVATE_COMPONENT = new DeeperWoundActivateComponent();

    private static class DeeperWoundActivateComponent implements EventActivatableComponent {
        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return event instanceof BleedActivateEvent bleedActivateEvent && !bleedActivateEvent.isCancelled();
        }
    }
}
