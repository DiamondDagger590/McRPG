package us.eunoians.mcrpg.skill.component.block;

import org.bukkit.GameMode;
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

/**
 * This component allows for a {@link us.eunoians.mcrpg.skill.Skill} to gain experience when breaking blocks.
 */
public interface OnBlockBreakLevelableComponent extends EventLevelableComponent {

    /**
     * Checks to see if this component can process the provided {@link Block}.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the provided {@link Block} can be processed by this component.
     */
    boolean affectsBlock(@NotNull Block block);

    @Override
    default boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        if (event instanceof BlockBreakEvent blockBreakEvent) {
            Player player = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();
            WorldManager worldManager = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
            return player.getGameMode() != GameMode.CREATIVE && !blockBreakEvent.isCancelled() && player.getUniqueId().equals(skillHolder.getUUID())
                    && affectsBlock(block) && worldManager.isMcRPGEnabledForHolder(skillHolder) && worldManager.isBlockNatural(block);
        }
        return false;
    }

    /**
     * Gets the amount of experience to award for the provided {@link Block} before any modifiers are applied.
     *
     * @param skillHolder The {@link SkillHolder} to get the amount of experience for.
     * @param block       The {@link Block} to get the base experience amount for.
     * @return The amount of experience to award for the provided {@link Block} before any modifiers are applied.
     */
    int getBaseExperienceForBlock(@NotNull SkillHolder skillHolder, @NotNull Block block);

    /**
     * Gets the amount of {@link Block}s that are going to be broken as a result of the provided block being broken.
     * <p>
     * This is typically expected for blocks that can grow vertically such as cacti. To see all supported types, see {@link MultiBlockType}.
     *
     * @param block The {@link Block} to get the affected block count from.
     * @return The amount of {@link Block}s that are going to be broken as a result of the provided block being broken.
     */
    default int getTotalAffectedBlocks(@NotNull Block block) {
        Optional<MultiBlockType> multiBlockTypeOptional = MultiBlockType.getMultiBlockType(block);
        return multiBlockTypeOptional.map(multiBlockType -> multiBlockType.calculateMultiBlockDrops(block)).orElse(1);
    }
}
