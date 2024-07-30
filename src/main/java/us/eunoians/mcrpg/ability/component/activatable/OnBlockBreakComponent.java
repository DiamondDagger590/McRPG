package us.eunoians.mcrpg.ability.component.activatable;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.world.WorldManager;

public interface OnBlockBreakComponent extends EventActivatableComponent {

    boolean affectsBlock(@NotNull Block block);

    @Override
    default boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        if (event instanceof BlockBreakEvent blockBreakEvent) {
            Entity entity = blockBreakEvent.getPlayer();
            Block block = blockBreakEvent.getBlock();
            return entity.getUniqueId().equals(abilityHolder.getUUID()) && affectsBlock(block) && WorldManager.isBlockNatural(block);
        }
        return false;
    }
}
