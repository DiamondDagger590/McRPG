package us.eunoians.mcrpg.api.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;

public class SkillRegisterEvent extends SkillEvent {

    private static final HandlerList handlers = new HandlerList();

    public SkillRegisterEvent(@NotNull Skill skill) {
        this(skill.getSkillKey());
    }

    public SkillRegisterEvent(@NotNull NamespacedKey skillKey) {
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