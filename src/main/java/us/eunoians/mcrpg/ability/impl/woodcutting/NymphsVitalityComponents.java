package us.eunoians.mcrpg.ability.impl.woodcutting;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Random;

/**
 * All the components needed to activate {@link NymphsVitality}.
 */
public class NymphsVitalityComponents {

    private static final Random RANDOM = new Random();
    public static final NymphsVitalityActivateOnHungerDropComponent NYMPHS_VITALITY_ACTIVATE_ON_HUNGER_DROP_COMPONENT = new NymphsVitalityActivateOnHungerDropComponent();
    public static final NymphsVitalityActivateOnMoveDropComponent NYMPHS_VITALITY_ACTIVATE_ON_MOVE_DROP_COMPONENT = new NymphsVitalityActivateOnMoveDropComponent();

    private static class NymphsVitalityActivateOnHungerDropComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (event instanceof FoodLevelChangeEvent foodLevelChangeEvent) {
                HumanEntity humanEntity = foodLevelChangeEvent.getEntity();
                NymphsVitality nymphsVitality = (NymphsVitality) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(NymphsVitality.NYMPHS_VITALITY_KEY);
                return humanEntity.getUniqueId().equals(abilityHolder.getUUID())
                        && humanEntity.getFoodLevel() <= nymphsVitality.getMinimumHunger(nymphsVitality.getCurrentAbilityTier(abilityHolder))
                        && nymphsVitality.isBiomeValid(humanEntity.getLocation().getBlock().getBiome());
            }
            return false;
        }
    }

    private static class NymphsVitalityActivateOnMoveDropComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (event instanceof PlayerMoveEvent playerMoveEvent) {
                Player player = playerMoveEvent.getPlayer();
                NymphsVitality nymphsVitality = (NymphsVitality) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(NymphsVitality.NYMPHS_VITALITY_KEY);
                return player.getUniqueId().equals(abilityHolder.getUUID())
                        && player.getFoodLevel() <= nymphsVitality.getMinimumHunger(nymphsVitality.getCurrentAbilityTier(abilityHolder))
                        && nymphsVitality.isBiomeValid(player.getLocation().getBlock().getBiome());
            }
            return false;
        }
    }
}
