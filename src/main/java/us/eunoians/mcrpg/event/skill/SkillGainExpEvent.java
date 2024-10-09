package us.eunoians.mcrpg.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

public class SkillGainExpEvent extends SkillEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SkillHolder skillHolder;
    private int exp;
    private boolean cancelled = false;

    public SkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int exp) {
        this(skillHolder, skill.getSkillKey(), exp);
    }

    public SkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, int exp) {
        super(skillKey);
        this.skillHolder = skillHolder;
        this.exp = Math.max(0, exp);
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
    public HandlerList getHandlers(){
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
