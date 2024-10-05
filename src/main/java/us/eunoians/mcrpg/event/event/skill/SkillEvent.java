package us.eunoians.mcrpg.event.event.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * This event is a base event for any event that is related to a {@link us.eunoians.mcrpg.skill.Skill}.
 */
public abstract class SkillEvent extends Event {

    private final NamespacedKey skillKey;

    public SkillEvent(@NotNull NamespacedKey skillKey) {
        this.skillKey = skillKey;
    }

    /**
     * Get the {@link NamespacedKey} of the {@link us.eunoians.mcrpg.skill.Skill} that is affected
     * by this event.
     *
     * @return The {@link NamespacedKey} of the {@link us.eunoians.mcrpg.skill.Skill} that is affected by
     * this event.
     */
    @NotNull
    public NamespacedKey getSkillKey() {
        return skillKey;
    }
}
