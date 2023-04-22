package us.eunoians.mcrpg.api.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

public class SkillGainLevelEvent extends SkillEvent {

    private static final HandlerList handlers = new HandlerList();

    private final SkillHolder skillHolder;
    private int levels;

    public SkillGainLevelEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, int levels) {
        super(skillKey);
        this.skillHolder = skillHolder;
        this.levels = Math.max(0, levels);
    }

    @NotNull
    public SkillHolder getSkillHolder() {
        return skillHolder;
    }

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = Math.max(0, levels);
    }

    @Override
    @NotNull
    public HandlerList getHandlers(){
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
