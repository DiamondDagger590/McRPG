package us.eunoians.mcrpg.skill.impl.woodcutting;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.component.OnBlockBreakLevelableComponent;

public class WoodcuttingSkillComponents {

    public static final WoodcuttingLevelOnBlockBreakComponent WOODCUTTING_LEVEL_ON_BLOCK_BREAK_COMPONENT = new WoodcuttingLevelOnBlockBreakComponent();

    private static final class WoodcuttingLevelOnBlockBreakComponent implements OnBlockBreakLevelableComponent {

        @Override
        public int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event) {
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event; //Safe cast since can only be called after checks are done
            Player player = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();

            player.getEquipment();
            ItemStack heldItem = player.getEquipment().getItemInMainHand();

            //TODO pull from config
            double expToAward = 1000;

            return (int) expToAward;

        }

        @Override
        public boolean affectsBlock(@NotNull Block block) {
            // TODO check
            return true;
        }

        @Override
        public boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {

            if (OnBlockBreakLevelableComponent.super.shouldGiveExperience(skillHolder, event)) {
                BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event; //Safe cast due to super call
                Player player = blockBreakEvent.getPlayer();
                Block block = blockBreakEvent.getBlock();

                player.getEquipment();
                ItemStack heldItem = player.getEquipment().getItemInMainHand();
                return heldItem.getType().name().contains("_AXE");
            }
            return false;
        }
    }
}
