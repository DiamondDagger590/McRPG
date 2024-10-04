package us.eunoians.mcrpg.skill.impl.woodcutting;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.skill.McRPGSkill;
import us.eunoians.mcrpg.skill.Skill;

/**
 * A {@link Skill} that focuses on the usage of breaking wood with an axe.
 * <p>
 * Players will gain experience by breaking wood with an axe and unlock abilities focused
 * on increasing the yield/ease of woodcutting.
 */
public class Woodcutting extends McRPGSkill {

    public static final NamespacedKey WOODCUTTING_KEY = new NamespacedKey(McRPG.getInstance(), "mining");

    public Woodcutting() {
        super(WOODCUTTING_KEY);
        addLevelableComponent(WoodcuttingSkillComponents.WOODCUTTING_LEVEL_ON_BLOCK_BREAK_COMPONENT, BlockBreakBlockEvent.class, 0);

    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Woodcutting";
    }

    @Override
    public int getMaxLevel() {
        return 1000;
    }
}
