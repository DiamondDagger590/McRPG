package us.eunoians.mcrpg.registry;

import com.diamonddagger590.mccore.registry.Registry;
import com.diamonddagger590.mccore.registry.RegistryKey;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;

import static com.diamonddagger590.mccore.registry.RegistryKeyImpl.create;

public interface McRPGRegistryKey extends RegistryKey<Registry<?>> {

    RegistryKey<AbilityRegistry> ABILITY = create(AbilityRegistry.class);
    RegistryKey<SkillRegistry> SKILL = create(SkillRegistry.class);
    RegistryKey<AbilityAttributeRegistry> ABILITY_ATTRIBUTE = create(AbilityAttributeRegistry.class);
    RegistryKey<ExperienceModifierRegistry> EXPERIENCE_MODIFIER = create(ExperienceModifierRegistry.class);
}
