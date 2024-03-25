package us.eunoians.mcrpg.api.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

public class PostSkillGainExpEvent extends SkillEvent {

    private static final HandlerList handlers = new HandlerList();

    private final SkillHolder skillHolder;

    public PostSkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull Skill skill) {
        this(skillHolder, skill.getSkillKey());
    }

    public PostSkillGainExpEvent(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey) {
        super(skillKey);
        this.skillHolder = skillHolder;
    }

    @NotNull
    public SkillHolder getSkillHolder() {
        return skillHolder;
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