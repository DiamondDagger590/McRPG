package us.eunoians.mcrpg.api.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

/**
 * This event is called after a player has gained levels in the {@link us.eunoians.mcrpg.skill.Skill}
 * associated with a given {@link NamespacedKey}
 */
public class PostSkillGainLevelEvent extends SkillEvent {

    private static final HandlerList handlers = new HandlerList();

    private final SkillHolder skillHolder;
    private final int beforeLevel, afterLevel;

    public PostSkillGainLevelEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, int beforeLevel, int afterLevel) {
        super(skillKey);
        this.skillHolder = skillHolder;
        this.beforeLevel = beforeLevel;
        this.afterLevel = afterLevel;
    }

    /**
     * Gets the {@link SkillHolder} that gained levels
     *
     * @return The {@link SkillHolder} that gained levels
     */
    @NotNull
    public SkillHolder getSkillHolder() {
        return skillHolder;
    }

    public int getBeforeLevel() {
        return beforeLevel;
    }

    public int getAfterLevel() {
        return afterLevel;
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
