package us.eunoians.mcrpg.skill.component;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

/**
 * This class provides a base for calculating skill experience to be awarded
 * to {@link SkillHolder}s.
 */
public interface EventLevelableComponent {

    /**
     * Checks to see if this component should give experience to the provided {@link SkillHolder}
     * from the provided {@link Event}.
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
