package us.eunoians.mcrpg.skill.experience.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.skill.experience.context.MockExperienceContext;
import us.eunoians.mcrpg.skill.experience.context.SkillExperienceContext;

/**
 * This mock modifier exists to validate behavior of {@link us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry}
 * in an agnostic way while not relying on an actually implemented modifier.
 */
public class MockModifier extends ExperienceModifier {

    private final NamespacedKey key;

    public MockModifier(@NotNull McRPG mcRPG) {
        key = new NamespacedKey(mcRPG, "skill");
    }

    @Override
    public NamespacedKey getModifierKey() {
        return key;
    }

    @Override
    public boolean canProcessContext(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext) {
        return skillExperienceContext instanceof MockExperienceContext;
    }

    @Override
    public double getModifier(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext, int experienceToCalculateOn) {
        return 10;
    }
}
