package us.eunoians.mcrpg.api.event.skill;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;

public class SkillUnregisterEvent extends SkillEvent {

    private static final HandlerList handlers = new HandlerList();

    public SkillUnregisterEvent(@NotNull Skill skill) {
        super(skill);
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
