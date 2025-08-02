package us.eunoians.mcrpg.skill.component;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

/**
 * This class provides a base for calculating skill experience to be awarded
 * to {@link SkillHolder}s.
 * <p>
 * Components are processed iteratively, with the expectation that components registered
 * will be sorted based on the priority defined in {@link EventLevelableComponentAttribute}.
 * Processing components in this way allows for the creation of expectations that can be fed down the
 * component chain, where if one component does validation on the type of {@link Event} being
 * used, that assumption becomes transitive.
 * <p>
 * This means a component has two responsibilities. The first is to determine "can this event award experience
 * even if that experience is 0?". If the answer to this is "no" for any component, then no component
 * can award experience for the event. This is different from "should this event award experience" which is the second
 * responsibility.
 * <p>
 * If every component in the chain agrees that the event can provide experience, then every component will offer
 * up an amount of experience that should be awarded from the event. The final value will be the highest from all
 * offered values.
 * <p>
 * If modifying the results of experience gain is desired, see {@link us.eunoians.mcrpg.skill.experience.modifier.ExperienceModifier}
 * to understand how to integrate with the modifier system.
 */
public interface EventLevelableComponent {

    /**
     * Checks to see if this component should give experience to the provided {@link SkillHolder}
     * from the provided {@link Event}.
     * <p>
     * There is a meaningful difference between "this event should return 0 experience" and "this
     * event should not give experience". The main differentiation is that if something is wrong with
     * the provided event such that no experience should be given, no matter what other components exist.
     * Otherwise, this component as a whole should just return a calculated experience amount of 0.
     *
     * @param skillHolder The {@link SkillHolder} to check if experience can be awarded to
     * @param event       The {@link Event} to check if experience can be awarded from
     * @return {@code true} if this component can award experience from the provided {@link Event}.
     */
    boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event);

    /**
     * Calculates the amount of experience that the provided {@link Event} can award the provided
     * {@link SkillHolder}.
     *
     * @param skillHolder The {@link SkillHolder} to calculate experience for
     * @param event       The {@link Event} to calculate experience from
     * @return A positive, zero inclusive amount of experience to be awarded.
     */
    int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event);
}
