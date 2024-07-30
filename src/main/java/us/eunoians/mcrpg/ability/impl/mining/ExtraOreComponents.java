package us.eunoians.mcrpg.ability.impl.mining;

import com.diamonddagger590.mccore.parser.Parser;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.OnBlockBreakComponent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
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
            if (abilityHolder instanceof SkillHolder skillHolder) {
                var skillHolderDataOptional = skillHolder.getSkillHolderData(Mining.MINING_KEY);
                if (skillHolderDataOptional.isPresent()) {
                    Parser parser = new Parser(McRPG.getInstance().getFileManager().getFile(FileType.MINING_CONFIG).getString(MiningConfigFile.EXTRA_ORE_ACTIVATION_EQUATION));
                    parser.setVariable("mining_level", skillHolderDataOptional.get().getCurrentLevel());
                    return parser.getValue() * 1000 > RANDOM.nextInt(100000);
                }
            }
            return false;
        }
    }
}
