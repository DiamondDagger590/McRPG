package us.eunoians.mcrpg.ability.component.activatable;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * An {@link EventActivatableComponent} which will activate for {@link BlockBreakEvent}s
 * and provides base behavior for abilities to build on surrounding the event.
 */
public interface OnBlockBreakComponent extends EventActivatableComponent {

    /**
     * Checks to see if the provided {@link Block} can trigger this component.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the provided {@link Block} can trigger this component.
     */
    boolean affectsBlock(@NotNull Block block);

    @Override
    default boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        if (event instanceof BlockBreakEvent blockBreakEvent) {
            Entity entity = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();
            return entity.getUniqueId().equals(abilityHolder.getUUID()) && affectsBlock(block) && McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD).isBlockNatural(block);
        }
        return false;
    }
}
