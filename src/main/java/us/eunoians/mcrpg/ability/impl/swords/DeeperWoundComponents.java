package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Random;

/**
 * The components that can activate {@link DeeperWound}
 */
public class DeeperWoundComponents {

    private static final Random RANDOM = new Random();
    public static final DeeperWoundActivateComponent DEEPER_WOUND_ACTIVATE_COMPONENT = new DeeperWoundActivateComponent();

    private static class DeeperWoundActivateComponent implements EventActivatableComponent {
        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (event instanceof BleedActivateEvent bleedActivateEvent && !bleedActivateEvent.isCancelled()) {
                AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
                DeeperWound deeperWound = (DeeperWound) abilityRegistry.getRegisteredAbility(DeeperWound.DEEPER_WOUND_KEY);
                double activationChance = deeperWound.getActivationChance(deeperWound.getCurrentAbilityTier(abilityHolder));
                return bleedActivateEvent.getAbilityHolder().equals(abilityHolder) && activationChance * 1000 > RANDOM.nextInt(100000);
            }
            return false;
        }
    }
}
