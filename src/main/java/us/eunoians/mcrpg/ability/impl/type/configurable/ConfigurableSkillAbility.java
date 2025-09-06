package us.eunoians.mcrpg.ability.impl.type.configurable;

import us.eunoians.mcrpg.ability.impl.type.SkillAbility;

/**
 * This interface represents an {@link us.eunoians.mcrpg.ability.Ability} that
 * belongs to a {@link us.eunoians.mcrpg.skill.Skill} and is configurable
 * in order to provide a more robust enablement check.
 */
public interface ConfigurableSkillAbility extends SkillAbility, ConfigurableAbility {

    @Override
    default boolean isAbilityEnabled() {
        return ConfigurableAbility.super.isAbilityEnabled() && SkillAbility.super.isAbilityEnabled();
    }
}
