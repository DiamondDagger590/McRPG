package us.eunoians.mcrpg.skill.component.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.component.EventLevelableComponent;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.Optional;

public interface OnBlockBreakLevelableComponent extends EventLevelableComponent {

    boolean affectsBlock(@NotNull Block block);

    @Override
    default boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        if (event instanceof BlockBreakEvent blockBreakEvent) {
            Player player = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();
            WorldManager worldManager = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
            return !blockBreakEvent.isCancelled() && player.getUniqueId().equals(skillHolder.getUUID()) && affectsBlock(block)
                    && worldManager.isMcRPGEnabledForHolder(skillHolder) && worldManager.isBlockNatural(block);
        }
        return false;
    }

    int getBaseExperienceForBlock(@NotNull SkillHolder skillHolder, @NotNull Block block);

    default int getTotalAffectedBlocks(@NotNull Block block) {
        Optional<MultiBlockType> multiBlockTypeOptional = MultiBlockType.getMultiBlockType(block);
        return multiBlockTypeOptional.map(multiBlockType -> multiBlockType.calculateMultiBlockDrops(block)).orElse(1);
    }
}
