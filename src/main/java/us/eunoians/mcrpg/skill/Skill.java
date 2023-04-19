package us.eunoians.mcrpg.skill;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public abstract class Skill {

    private final NamespacedKey skillKey;

    public Skill(@NotNull NamespacedKey skillKey) {
        this.skillKey = skillKey;
    }

    @NotNull
    public NamespacedKey getSkillKey() {
        return skillKey;
    }
}
