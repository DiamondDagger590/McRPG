package us.eunoians.mcrpg.skill.impl.mining;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.skill.Skill;

public class Mining extends Skill {

    public static final NamespacedKey MINING_KEY = new NamespacedKey(McRPG.getInstance(), "mining");

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
