package us.eunoians.mcrpg.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;

/**
 * This event is fired whenever a {@link Skill} is registered to the
 * {@link us.eunoians.mcrpg.skill.SkillRegistry} and is available for use.
 */
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