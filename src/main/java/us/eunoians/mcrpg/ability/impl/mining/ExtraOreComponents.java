package us.eunoians.mcrpg.ability.impl.mining;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.OnBlockBreakComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.Random;

public class ExtraOreComponents {

    private static final Random RANDOM = new Random();
    public static final ExtraOreOnBreakComponent EXTRA_ORE_ON_BREAK_COMPONENT = new ExtraOreOnBreakComponent();

    private static class ExtraOreOnBreakComponent implements OnBlockBreakComponent {

        @Override
        public boolean affectsBlock(@NotNull Block block) {
            return ((ExtraOre) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(ExtraOre.EXTRA_ORE_KEY)).isBlockValid(block);
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (!OnBlockBreakComponent.super.shouldActivate(abilityHolder, event)) {
                return false;
            }
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
            Player player = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();
            ExtraOre extraOre = (ExtraOre) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(ExtraOre.EXTRA_ORE_KEY);
            if (abilityHolder instanceof SkillHolder skillHolder) {
                var skillHolderDataOptional = skillHolder.getSkillHolderData(Mining.MINING_KEY);
                if (skillHolderDataOptional.isPresent()) {
                    return extraOre.getActivationChance(skillHolder) * 1000 > RANDOM.nextInt(100000);
                }
            }
            return false;
        }
    }
}
