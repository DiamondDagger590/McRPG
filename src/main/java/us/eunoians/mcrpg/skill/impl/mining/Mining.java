package us.eunoians.mcrpg.skill.impl.mining;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.McRPGSkill;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * A {@link Skill} that focuses on the usage of pickaces to mine.
 * <p>
 * Players will gain experience by mining ores and stone and unlock abilities focused
 * on increasing the yield/ease of mining.
 */
public final class Mining extends McRPGSkill {

    public static final NamespacedKey MINING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mining");

    public Mining() {
        super(MINING_KEY);
        addLevelableComponent(MiningSkillComponents.MINING_LEVEL_ON_BLOCK_BREAK_COMPONENT, BlockBreakBlockEvent.class, 0);
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Mining";
    }

    @Override
    public int getMaxLevel() {
        return 1000;
    }
}
