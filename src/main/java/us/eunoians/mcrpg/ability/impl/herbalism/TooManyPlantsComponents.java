package us.eunoians.mcrpg.ability.impl.herbalism;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.OnBlockBreakComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGAbilityKey;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;

import java.util.Random;

public class TooManyPlantsComponents {

    private static final Random RANDOM = new Random();
    public static final TooManyPlantsOnBreakComponent TOO_MANY_PLANTS_ON_BREAK_COMPONENT = new TooManyPlantsOnBreakComponent();

    private static class TooManyPlantsOnBreakComponent implements OnBlockBreakComponent {

        @Override
        public boolean affectsBlock(@NotNull Block block) {
            return McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).ability(McRPGAbilityKey.TOO_MANY_PLANTS).isBlockValid(block);
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (!OnBlockBreakComponent.super.shouldActivate(abilityHolder, event)) {
                return false;
            }
            TooManyPlants tooManyPlants = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).ability(McRPGAbilityKey.TOO_MANY_PLANTS);
            if (abilityHolder instanceof SkillHolder skillHolder) {
                var skillHolderDataOptional = skillHolder.getSkillHolderData(Herbalism.HERBALISM_KEY);
                if (skillHolderDataOptional.isPresent()) {
                    return tooManyPlants.getActivationChance(skillHolder) * 1000 > RANDOM.nextInt(100000);
                }
            }
            return false;
        }
    }
}
