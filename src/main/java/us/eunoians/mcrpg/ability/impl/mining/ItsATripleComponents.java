package us.eunoians.mcrpg.ability.impl.mining;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.api.event.ability.mining.ExtraOreActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Random;

public class ItsATripleComponents {

    private static final Random RANDOM = new Random();
    public static final ItsATripleActivateOnExtraDropComponent ITS_A_TRIPLE_ACTIVATE_ON_EXTRA_DROP_COMPONENT = new ItsATripleActivateOnExtraDropComponent();

    private static class ItsATripleActivateOnExtraDropComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (event instanceof ExtraOreActivateEvent extraOreActivateEvent) {
                ItsATriple itsATriple = (ItsATriple) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(ItsATriple.ITS_A_TRIPLE_KEY);
                return extraOreActivateEvent.getAbilityHolder().getUUID().equals(abilityHolder.getUUID())
                        && itsATriple.getActivationChance(itsATriple.getCurrentAbilityTier(abilityHolder)) * 1000 > RANDOM.nextInt(100000);
            }
            return false;
        }
    }
}
