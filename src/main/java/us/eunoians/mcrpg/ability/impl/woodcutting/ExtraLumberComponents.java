package us.eunoians.mcrpg.ability.impl.woodcutting;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.OnBlockBreakComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting;

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
            return ((ExtraLumber) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(ExtraLumber.EXTRA_LUMBER_KEY)).isBlockValid(block);
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (!OnBlockBreakComponent.super.shouldActivate(abilityHolder, event)) {
                return false;
            }
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
            Player player = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();
            ExtraLumber extraLumber = (ExtraLumber) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(ExtraLumber.EXTRA_LUMBER_KEY);
            if (abilityHolder instanceof SkillHolder skillHolder) {
                var skillHolderDataOptional = skillHolder.getSkillHolderData(Woodcutting.WOODCUTTING_KEY);
                if (skillHolderDataOptional.isPresent()) {
                    return extraLumber.getActivationChance(skillHolder) * 1000 > RANDOM.nextInt(100000);
                }
            }
            return false;
        }
    }
}
