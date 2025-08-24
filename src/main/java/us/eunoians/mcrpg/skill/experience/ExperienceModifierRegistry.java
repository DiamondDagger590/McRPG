package us.eunoians.mcrpg.skill.experience;

import com.diamonddagger590.mccore.registry.Registry;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.apiguardian.api.API;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
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
        double modifier = 1.0;
        int baseExperience = skillExperienceContext.getBaseExperience();
        // Do multiplication on the base experience before additive
        for  (ExperienceModifier experienceModifier : experienceModifiers) {
            if (!experienceModifier.isAdditive() && experienceModifier.canProcessContext(skillExperienceContext)) {
                double multiplierModifier = experienceModifier.getModifier(skillExperienceContext, baseExperience);
                baseExperience = (int) (baseExperience * multiplierModifier);
                modifier *= multiplierModifier;
            }
        }
        // Add to the multiplier (has to come after so the base experience being passed in is accurate
        double additiveModifier = 0d;
        boolean first = false;
        for (ExperienceModifier experienceModifier : experienceModifiers) {
            if (experienceModifier.isAdditive() && experienceModifier.canProcessContext(skillExperienceContext)) {
                additiveModifier += experienceModifier.getModifier(skillExperienceContext, baseExperience);
            }
        }
        modifier *= (additiveModifier == 0 ? 1 :additiveModifier);
        return modifier;
    }

    /**
     * This function only exists to be used by unit tests to reset the internals
     * of this registry.
     */
    @API(status = API.Status.INTERNAL)
    private static void reset() {
        RegistryAccess.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER).experienceModifiers.clear();
    }
}
