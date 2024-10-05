package us.eunoians.mcrpg.event.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;

public class SkillUnregisterEvent extends SkillEvent {

    private static final HandlerList handlers = new HandlerList();

    public SkillUnregisterEvent(@NotNull Skill skill) {
        this(skill.getSkillKey());
    }

    public SkillUnregisterEvent(@NotNull NamespacedKey skillKey) {
        super(skillKey);
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
