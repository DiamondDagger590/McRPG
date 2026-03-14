package us.eunoians.mcrpg.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.experience.context.GainReason;
import us.eunoians.mcrpg.skill.experience.context.McRPGGainReason;

/**
 * Fired <b>after</b> a {@link SkillHolder} has successfully gained experience in a skill.
 * <p>
 * This event is <b>not</b> cancellable — the experience has already been applied when this
 * event fires. Use it for read-only reactions such as updating statistics, triggering
 * displays, or logging.
 * <p>
 * To intercept or modify experience <i>before</i> it is applied, listen to
 * {@link SkillGainExpEvent} instead.
 *
 * @see SkillGainExpEvent
 */
public class PostSkillGainExpEvent extends SkillEvent {

    private static final HandlerList handlers = new HandlerList();

    private final SkillHolder skillHolder;
    private final int experience;
    private final GainReason gainReason;

    public PostSkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull Skill skill) {
        this(skillHolder, skill.getSkillKey(), 0, McRPGGainReason.OTHER);
    }

    public PostSkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey) {
        this(skillHolder, skillKey, 0, McRPGGainReason.OTHER);
    }

    public PostSkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, int experience, @NotNull GainReason gainReason) {
        super(skillKey);
        this.skillHolder = skillHolder;
        this.experience = experience;
        this.gainReason = gainReason;
    }

    @NotNull
    public SkillHolder getSkillHolder() {
        return skillHolder;
    }

    /**
     * Gets the amount of experience that was gained.
     *
     * @return The experience gained.
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Gets the {@link GainReason} describing why this experience was gained.
     *
     * @return The gain reason.
     */
    @NotNull
    public GainReason getGainReason() {
        return gainReason;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
