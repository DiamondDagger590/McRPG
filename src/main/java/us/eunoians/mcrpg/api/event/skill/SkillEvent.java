package us.eunoians.mcrpg.api.event.skill;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;

public abstract class SkillEvent extends Event {

    private final Skill skill;

    public SkillEvent(@NotNull Skill skill) {
        this.skill = skill;
    }

    @NotNull
    public Skill getSkill() {
        return skill;
    }
}
