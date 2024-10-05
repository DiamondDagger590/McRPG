package us.eunoians.mcrpg.event.event.ability.woodcutting;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.event.fake.FakeBlockBreakEvent;

/**
 * This event is type of {@link FakeBlockBreakEvent} fired whenever {@link us.eunoians.mcrpg.ability.impl.woodcutting.HeavySwing}
 * activates in order to check for block protections.
 */
public class HeavySwingFakeBlockBreakEvent extends FakeBlockBreakEvent {

    public HeavySwingFakeBlockBreakEvent(@NotNull Player player, @NotNull Block block) {
        super(block, player);
    }
}
