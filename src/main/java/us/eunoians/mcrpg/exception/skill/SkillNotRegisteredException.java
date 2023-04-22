package us.eunoians.mcrpg.exception.skill;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public class SkillNotRegisteredException extends RuntimeException{

    private final NamespacedKey skillKey;

    public SkillNotRegisteredException(@NotNull NamespacedKey skillKey) {
        this.skillKey = skillKey;
    }

    @NotNull
    public NamespacedKey getSkillKey() {
        return skillKey;
    }
}
