package us.eunoians.mcrpg.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

/**
 * This event is called whenever a player gains a skill level
 */
public class SkillGainLevelEvent extends SkillEvent {

    private static final HandlerList handlers = new HandlerList();

    private final SkillHolder skillHolder;
    private int levels;

    public SkillGainLevelEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, int levels) {
        super(skillKey);
        this.skillHolder = skillHolder;
        this.levels = Math.max(0, levels);
    }

    /**
     * Gets the {@link SkillHolder} that is gaining levels
     *
     * @return The {@link SkillHolder} that is gaining levels
     */
    @NotNull
    public SkillHolder getSkillHolder() {
        return skillHolder;
    }

    /**
     * Get the amount of levels that are being gained
     *
     * @return The amount of levels being gained
     */
    public int getLevels() {
        return levels;
    }

    /**
     * Sets the amount of levels that are being gained
     *
     * @param levels The amount of levels being gained. Must be 0 or greater
     */
    public void setLevels(int levels) {
        this.levels = Math.max(0, levels);
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
