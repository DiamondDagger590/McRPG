package us.eunoians.mcrpg.api.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

/**
 * This event is called after a player has gained levels in the {@link us.eunoians.mcrpg.skill.Skill}
 * associated with a given {@link NamespacedKey}
 */
public class PostSkillGainLevelEvent extends SkillEvent{

    private static final HandlerList handlers = new HandlerList();

    private final SkillHolder skillHolder;

    public PostSkillGainLevelEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey) {
        super(skillKey);
        this.skillHolder = skillHolder;
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
