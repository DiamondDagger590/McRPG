package us.eunoians.mcrpg.skill.experience.context;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

/**
 * A mock context that exists only to allow testing the {@link us.eunoians.mcrpg.skill.experience.modifier.MockModifier}
 * functionality in the {@link us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry}.
 */
public class MockExperienceContext extends SkillExperienceContext<Event> {

    public MockExperienceContext(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int baseExperience, @NotNull Event event) {
        super(skillHolder, skill, baseExperience, event);
    }

    @Override
    @NotNull
    public GainReason getGainReason() {
        return McRPGGainReason.OTHER;
    }
}
