package us.eunoians.mcrpg.external.papi.placeholder;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.external.papi.McRPGPapiExpansion;
import us.eunoians.mcrpg.external.papi.placeholder.ability.AbilityTierPlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.skill.SkillCurrentExperiencePlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.skill.SkillCurrentLevelPlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.skill.SkillRemainingExperiencePlaceholder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

public enum McRPGPlaceHolderType {

    SKILL_CURRENT_LEVEL((mcRPG, mcRPGPapiExpansion) -> {
        mcRPG.registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkillKeys().forEach(skillKey -> {
            mcRPGPapiExpansion.registerPlaceholder(new SkillCurrentLevelPlaceholder(skillKey));
        });
    }),
    SKILL_CURRENT_EXPERIENCE((mcRPG, mcRPGPapiExpansion) -> {
        mcRPG.registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkillKeys().forEach(skillKey -> {
            mcRPGPapiExpansion.registerPlaceholder(new SkillCurrentExperiencePlaceholder(skillKey));
        });
    }),
    SKILL_REMAINING_EXPERIENCE((mcRPG, mcRPGPapiExpansion) -> {
        mcRPG.registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkillKeys().forEach(skillKey -> {
            mcRPGPapiExpansion.registerPlaceholder(new SkillRemainingExperiencePlaceholder(skillKey));
        });
    }),
    ABILITY_TIER((mcRPG, mcRPGPapiExpansion) -> {
        AbilityRegistry abilityRegistry = mcRPG.registryAccess().registry(McRPGRegistryKey.ABILITY);
        abilityRegistry.getAllAbilities().forEach(abilityKey -> {
            if (abilityRegistry.getRegisteredAbility(abilityKey).getApplicableAttributes().contains(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY)) {
                mcRPGPapiExpansion.registerPlaceholder(new AbilityTierPlaceholder(abilityKey));
            }
        });
    }),
    ;

    private final PlaceholderRegisterFunction placeholderRegisterFunction;

    McRPGPlaceHolderType(@NotNull PlaceholderRegisterFunction placeholderRegisterFunction) {
        this.placeholderRegisterFunction = placeholderRegisterFunction;
    }

    public void registerPlaceholders(@NotNull McRPG mcRPG, @NotNull McRPGPapiExpansion mcRPGPapiExpansion) {
        placeholderRegisterFunction.registerPlaceholders(mcRPG, mcRPGPapiExpansion);
    }
}
