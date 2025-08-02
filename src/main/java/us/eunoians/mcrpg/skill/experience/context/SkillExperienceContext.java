package us.eunoians.mcrpg.skill.experience.context;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

/**
 * Skill Experience Contexts serve as a "snapshot" of sorts about an
 * {@link Event} that is going to be causing a {@link SkillHolder} to gain
 * experience in a {@link Skill}.
 * <p>
 * All three of those are contained in this context, which will be consumed by
 * {@link us.eunoians.mcrpg.skill.experience.modifier.ExperienceModifier}s registered to
 * the {@link us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry}.
 *
 * @param <T> The {@link Event} that is going to be giving experience.
 */
public abstract class SkillExperienceContext<T extends Event> {

    private final SkillHolder skillHolder;
    private final Skill skill;
    private final int baseExperience;
    private final T event;

    public SkillExperienceContext(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int baseExperience, @NotNull T event) {
        this.skillHolder = skillHolder;
        this.skill = skill;
        this.baseExperience = Math.max(0, baseExperience);
        this.event = event;
    }

    /**
     * Gets the {@link SkillHolder} that is going to be gaining experience.
     *
     * @return The {@link SkillHolder} that is going to be gaining experience.
     */
    @NotNull
    public SkillHolder getSkillHolder() {
        return skillHolder;
    }

    /**
     * Gets the {@link T} that is going to be awarding experience.
     *
     * @return The {@link T} that is going to be awarding experience.
     */
    @NotNull
    public T getEvent() {
        return event;
    }

    /**
     * The {@link Skill} that is going to be gaining experience.
     *
     * @return The {@link Skill} that is going to be gaining experience.
     */
    @NotNull
    public Skill getSkill() {
        return skill;
    }

    /**
     * Gets the base amount of experience that is being given in this context.
     *
     * @return The base amount of experience that is being given in this context.
     */
    public final int getBaseExperience() {
        return baseExperience;
    }
}
