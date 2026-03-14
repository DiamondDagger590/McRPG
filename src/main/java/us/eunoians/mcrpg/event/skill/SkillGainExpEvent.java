package us.eunoians.mcrpg.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.experience.context.GainReason;
import us.eunoians.mcrpg.skill.experience.context.McRPGGainReason;

/**
 * Fired <b>before</b> a {@link SkillHolder} gains experience in a skill.
 * <p>
 * This event is {@link Cancellable} — cancelling it prevents the experience from being
 * awarded. Listeners may also modify the experience amount via {@link #setExperience(int)}.
 * <p>
 * After the experience has been successfully applied, a corresponding
 * {@link PostSkillGainExpEvent} is fired.
 *
 * @see PostSkillGainExpEvent
 */
public class SkillGainExpEvent extends SkillEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SkillHolder skillHolder;
    private final GainReason gainReason;
    private int exp;
    private boolean cancelled = false;

    public SkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int exp) {
        this(skillHolder, skill.getSkillKey(), exp, McRPGGainReason.OTHER);
    }

    public SkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, int exp) {
        this(skillHolder, skillKey, exp, McRPGGainReason.OTHER);
    }

    public SkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, int exp, @NotNull GainReason gainReason) {
        super(skillKey);
        this.skillHolder = skillHolder;
        this.exp = Math.max(0, exp);
        this.gainReason = gainReason;
    }

    @NotNull
    public SkillHolder getSkillHolder() {
        return skillHolder;
    }

    public int getExperience() {
        return exp;
    }

    public void setExperience(int experience) {
        exp = Math.max(0, experience);
    }

    /**
     * Gets the {@link GainReason} describing why this experience is being gained.
     *
     * @return The gain reason.
     */
    @NotNull
    public GainReason getGainReason() {
        return gainReason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
