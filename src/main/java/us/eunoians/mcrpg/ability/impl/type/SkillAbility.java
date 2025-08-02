package us.eunoians.mcrpg.ability.impl.type;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;

/**
 * This interface signifies that an {@link Ability} belongs
 * to a {@link us.eunoians.mcrpg.skill.Skill}. If a given ability
 * is not of this interface, then it can be assumed it doesn't have a
 * skill it is tied to for things like progressing or unlocking.
 */
public interface SkillAbility extends Ability {

    /**
     * Gets the {@link NamespacedKey} for the {@link us.eunoians.mcrpg.skill.Skill}
     * this ability belongs to.
     *
     * @return The {@link NamespacedKey} for the {@link us.eunoians.mcrpg.skill.Skill}
     * this ability belongs to.
     */
    @NotNull
    NamespacedKey getSkillKey();
}
