package us.eunoians.mcrpg.exception.entity;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

/**
 * This exception is thrown whenever a {@link SkillHolder} does not have {@link us.eunoians.mcrpg.entity.holder.SkillHolder.SkillHolderData}
 * for a given {@link us.eunoians.mcrpg.skill.Skill}.
 */
public class SkillHolderMissingSkillException extends RuntimeException {

    private final SkillHolder skillHolder;
    private final NamespacedKey skillKey;

    public SkillHolderMissingSkillException(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey) {
        this.skillHolder = skillHolder;
        this.skillKey = skillKey;
    }

    public SkillHolderMissingSkillException(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, @NotNull String message) {
        super(message);
        this.skillHolder = skillHolder;
        this.skillKey = skillKey;
    }

    /**
     * Gets the {@link SkillHolder} that did not have data for a given {@link us.eunoians.mcrpg.skill.Skill}.
     *
     * @return The {@link SkillHolder} that did not have data for a given {@link us.eunoians.mcrpg.skill.Skill}.
     */
    @NotNull
    public SkillHolder getSkillHolder() {
        return skillHolder;
    }

    /**
     * The {@link NamespacedKey} of the {@link us.eunoians.mcrpg.skill.Skill} that was missing data.
     *
     * @return The {@link NamespacedKey} of the {@link us.eunoians.mcrpg.skill.Skill} that was missing data.
     */
    @NotNull
    public NamespacedKey getSkillKey() {
        return skillKey;
    }
}
