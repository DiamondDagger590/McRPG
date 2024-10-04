package us.eunoians.mcrpg.ability.impl.woodcutting;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Random;

/**
 * All the components needed to activate {@link HeavySwing}.
 */
public class HeavySwingComponents {

    private static final Random RANDOM = new Random();
    public static final HeavySwingActivateComponent HEAVY_SWING_ACTIVATE_COMPONENT = new HeavySwingActivateComponent();

    private static class HeavySwingActivateComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (event instanceof BlockBreakEvent blockBreakEvent) {
                HeavySwing heavySwing = (HeavySwing) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(HeavySwing.HEAVY_SWING_KEY);
                return blockBreakEvent.getPlayer().getUniqueId().equals(abilityHolder.getUUID()) && heavySwing.isBlockValid(blockBreakEvent.getBlock())
                        && heavySwing.getActivationChance(heavySwing.getCurrentAbilityTier(abilityHolder)) * 1000 > RANDOM.nextInt(100000);
            }
            return false;
        }
    }
}
