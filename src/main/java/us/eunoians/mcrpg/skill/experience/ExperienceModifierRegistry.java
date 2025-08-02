package us.eunoians.mcrpg.skill.experience;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.skill.experience.context.SkillExperienceContext;
import us.eunoians.mcrpg.skill.experience.modifier.ExperienceModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * This registry is used for registering {@link ExperienceModifier}s to modify
 * experience gain.
 */
public final class ExperienceModifierRegistry implements Registry<ExperienceModifier> {

    private final McRPG mcRPG;
    private final List<ExperienceModifier> experienceModifiers;

    public ExperienceModifierRegistry(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        experienceModifiers = new ArrayList<>();
    }

    /**
     * Registers the provided {@link ExperienceModifier} to be used.
     *
     * @param experienceModifier The {@link ExperienceModifier} to register.
     */
    public void register(@NotNull ExperienceModifier experienceModifier) {
        experienceModifiers.add(experienceModifier);
    }

    @Override
    public boolean registered(@NotNull ExperienceModifier experienceModifier) {
        return experienceModifiers.contains(experienceModifier);
    }

    /**
     * Calculates the modifier that should be applied to experience gained from the provided {@link SkillExperienceContext}.
     *
     * @param skillExperienceContext The {@link SkillExperienceContext} to calculate a modifier for.
     * @return The modifier that should be applied to gained experience.
     */
    public double calculateModifierForContext(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext) {
        return experienceModifiers.stream()
                .filter(experienceModifier -> experienceModifier.canProcessContext(skillExperienceContext))
                .map(experienceModifier -> experienceModifier.getModifier(skillExperienceContext))
                .reduce(1.0, Double::sum);
    }
}
