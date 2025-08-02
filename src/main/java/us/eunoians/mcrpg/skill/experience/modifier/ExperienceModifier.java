package us.eunoians.mcrpg.skill.experience.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.experience.context.SkillExperienceContext;

/**
 * An Experience Modifier will provide various modifiers to experience gained, allowing
 * for boosts or reductions in the total experience gained.
 * <p>
 * Modifiers are registered in the {@link us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry} and
 * are processed iteratively with the summation of modifiers being the multiplier that gets applied to the experience
 * gain.
 * <p>
 * A Modifier returning a result of {@code 0.0} means that there is no change from this modifier. A positive
 * result will be added to the final result while a negative will be subtracted. This means if there are places
 * for configuration which represent no change with {@code 1.0} to be user readable, then that needs to be normalized for
 * the expectation of {@code 0.0} meaning no change in this system.
 */
public abstract class ExperienceModifier {

    /**
     * Gets the {@link NamespacedKey} that uniquely identifies this modifier.
     *
     * @return The {@link NamespacedKey} that uniquely identifies this modifier.
     */
    public abstract NamespacedKey getModifierKey();

    /**
     * Checks to see if the provided {@link SkillExperienceContext} can be processed by this modifier.
     *
     * @param skillExperienceContext The {@link SkillExperienceContext} to validate.
     * @return {@code true} if the provided {@link SkillExperienceContext} can be processed by this modifier.
     */
    public abstract boolean canProcessContext(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext);

    /**
     * Gets the total modifier that experience gain should be increased by for this modifier.
     * <p>
     * As all modifiers are summed, a result of {@code 0.0} means no change, while {@code 1.0} means to increase
     * the experience gain by 100%. Returning a value such as {@code -0.25} means to reduce the experience gain by
     * 25%.
     * <p>
     * This method assumes that the passed in {@link SkillExperienceContext} passed the check of {@link #canProcessContext(SkillExperienceContext)}.
     *
     * @param skillExperienceContext The {@link SkillExperienceContext} to consume to calculate modifier total from.
     * @return The total modifier that experience gain should be increased by for this modifier. A return value of {@code 0.0}
     * means no change, a positive result will increase the total modifier amount and a negative result will decrease the total modifier
     */
    public abstract double getModifier(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext);
}
