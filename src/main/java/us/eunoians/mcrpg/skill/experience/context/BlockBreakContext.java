package us.eunoians.mcrpg.skill.experience.context;

import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

/**
 * A {@link SkillExperienceContext} that contains the {@link BlockBreakEvent}
 * to be processed for {@link us.eunoians.mcrpg.skill.experience.modifier.ExperienceModifier}s.
 */
public class BlockBreakContext extends SkillExperienceContext<BlockBreakEvent> {

    public BlockBreakContext(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int baseExperience, @NotNull BlockBreakEvent event) {
        super(skillHolder, skill, baseExperience, event);
    }
}
