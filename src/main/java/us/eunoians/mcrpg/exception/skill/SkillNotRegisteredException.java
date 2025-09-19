package us.eunoians.mcrpg.exception.skill;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown whenever a {@link us.eunoians.mcrpg.skill.Skill} is referenced
 * while not being registered.
 */
public class SkillNotRegisteredException extends RuntimeException {

    private final NamespacedKey skillKey;

    public SkillNotRegisteredException(@NotNull NamespacedKey skillKey) {
        this.skillKey = skillKey;
    }

    public SkillNotRegisteredException(@NotNull NamespacedKey skillKey, @NotNull String message) {
        super(message);
        this.skillKey = skillKey;
    }

    /**
     * This {@link NamespacedKey} of the {@link us.eunoians.mcrpg.skill.Skill} that threw the
     * exception.
     *
     * @return The {@link NamespacedKey} of the {@link us.eunoians.mcrpg.skill.Skill} that threw the exception.
     */
    @NotNull
    public NamespacedKey getSkillKey() {
        return skillKey;
    }
}
