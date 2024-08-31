package us.eunoians.mcrpg.ability.impl;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Set;

/**
 * Any ability that will be unlocked through skill level up should extend this.
 * <p>
 * It provides a default set of applicable attributes that all unlockable abilities should
 * use.
 */
public interface UnlockableAbility extends Ability {

    /**
     * Gets the level that this ability can be unlocked at.
     *
     * @return The level that this ability can be unlocked at.
     */
    int getUnlockLevel();

    /**
     * Checks to see if this ability can currently be unlocked for the given {@link SkillHolder} with their given
     * {@link Skill}.
     * @param skillHolder The {@link SkillHolder} to check against
     * @param skill The {@link Skill} to use to check if this ability can be unlocked.
     * @return {@code true} if this ability can currently be unlocked.
     */
    default boolean checkIfAbilityCanBeUnlocked(@NotNull SkillHolder skillHolder, @NotNull Skill skill) {
        var skillDataOptional = skillHolder.getSkillHolderData(skill);
        if (skillDataOptional.isPresent()) {
            var skillData = skillDataOptional.get();
            return skillData.getCurrentLevel() >= getUnlockLevel();
        }
        return false;
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE);
    }

    default boolean isAbilityUnlocked(@NotNull AbilityHolder abilityHolder) {
        var abilityDataOptional = abilityHolder.getAbilityData(this);
        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            var attributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE);
            if (attributeOptional.isPresent() && attributeOptional.get() instanceof AbilityUnlockedAttribute attribute) {
                return attribute.getContent();
            }
        }
        return false;
    }
}
