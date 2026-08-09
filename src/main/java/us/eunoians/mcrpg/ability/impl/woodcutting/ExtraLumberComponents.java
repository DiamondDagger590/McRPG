package us.eunoians.mcrpg.ability.impl.woodcutting;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.OnBlockBreakComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGAbilityKey;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;

import java.util.Random;

/**
 * All the components needed to activate {@link ExtraLumber}.
 */
public class ExtraLumberComponents {

    private static final Random RANDOM = new Random();
    public static final ExtraLumberOnBreakComponent EXTRA_LUMBER_ON_BREAK_COMPONENT = new ExtraLumberOnBreakComponent();

    private static class ExtraLumberOnBreakComponent implements OnBlockBreakComponent {

        @Override
        public boolean affectsBlock(@NotNull Block block) {
            return McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).ability(McRPGAbilityKey.EXTRA_LUMBER).isBlockValid(block);
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (!OnBlockBreakComponent.super.shouldActivate(abilityHolder, event)) {
                return false;
            }
            ExtraLumber extraLumber = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).ability(McRPGAbilityKey.EXTRA_LUMBER);
            if (abilityHolder instanceof SkillHolder skillHolder) {
                var skillHolderDataOptional = skillHolder.getSkillHolderData(WoodCutting.WOODCUTTING_KEY);
                if (skillHolderDataOptional.isPresent()) {
                    return extraLumber.getActivationChance(skillHolder) * 1000 > RANDOM.nextInt(100000);
                }
            }
            return false;
        }
    }
}
