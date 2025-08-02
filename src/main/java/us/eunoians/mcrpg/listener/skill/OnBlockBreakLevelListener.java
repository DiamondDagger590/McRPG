package us.eunoians.mcrpg.listener.skill;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.experience.context.BlockBreakContext;
import us.eunoians.mcrpg.skill.experience.context.SkillExperienceContext;

import java.util.Optional;

/**
 * This listener handles {@link Skill}s that level up based on the {@link BlockBreakEvent}.
 */
public class OnBlockBreakLevelListener implements SkillListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        levelSkill(event.getPlayer().getUniqueId(), event);
    }

    @NotNull
    @Override
    public Optional<SkillExperienceContext<?>> getEventContext(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int experience, @NotNull Event event) {
        return event instanceof BlockBreakEvent blockBreakEvent ? Optional.of(new BlockBreakContext(skillHolder, skill, experience, blockBreakEvent)) : Optional.empty();
    }
}
