package us.eunoians.mcrpg.skill.component;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

public interface OnBlockBreakLevelableComponent extends EventLevelableComponent {

    boolean affectsBlock(@NotNull Block block);

    @Override
    default boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        if (event instanceof BlockBreakEvent blockBreakEvent) {
            Player player = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();
            return player.getUniqueId().equals(skillHolder.getUUID()) && affectsBlock(block) && McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD).isBlockNatural(block);
        }
        return false;
    }
}
