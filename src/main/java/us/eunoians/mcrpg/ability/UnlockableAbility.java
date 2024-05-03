package us.eunoians.mcrpg.ability;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Set;

/**
 * Any ability that will be unlocked through skill level up should extend this.
 * <p>
 * It provides a default set of applicable attributes that all unlockable abilities should
 * use.
 */
public abstract class UnlockableAbility extends Ability {

    public UnlockableAbility(@NotNull NamespacedKey abilityKey) {
        super(abilityKey);
    }

    /**
     * Gets the level that this ability can be unlocked at.
     *
     * @return The level that this ability can be unlocked at.
     */
    public abstract int getUnlockLevel();

    /**
     *
     * @param skillHolder
     * @param skill
     * @return
     */
    public boolean checkIfAbilityCanBeUnlocked(@NotNull SkillHolder skillHolder, @NotNull Skill skill) {
        var skillDataOptional = skillHolder.getSkillHolderData(skill);
        if (skillDataOptional.isPresent()) {
            var skillData = skillDataOptional.get();
            return skillData.getCurrentLevel() >= getUnlockLevel();
        }
        return false;
    }

    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE);
    }
}
