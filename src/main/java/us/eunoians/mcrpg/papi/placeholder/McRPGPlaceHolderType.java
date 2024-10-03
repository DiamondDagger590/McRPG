package us.eunoians.mcrpg.papi.placeholder;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.papi.McRPGPapiExpansion;
import us.eunoians.mcrpg.papi.placeholder.ability.AbilityTierPlaceholder;
import us.eunoians.mcrpg.papi.placeholder.skill.SkillCurrentExperiencePlaceholder;
import us.eunoians.mcrpg.papi.placeholder.skill.SkillCurrentLevelPlaceholder;
import us.eunoians.mcrpg.papi.placeholder.skill.SkillRemainingExperiencePlaceholder;

public enum McRPGPlaceHolderType {

    SKILL_CURRENT_LEVEL((mcRPG, mcRPGPapiExpansion) -> {
        mcRPG.getSkillRegistry().getRegisteredSkillKeys().forEach(skillKey -> {
            mcRPGPapiExpansion.registerPlaceholder(new SkillCurrentLevelPlaceholder(skillKey));
        });
    }),
    SKILL_CURRENT_EXPERIENCE((mcRPG, mcRPGPapiExpansion) -> {
        mcRPG.getSkillRegistry().getRegisteredSkillKeys().forEach(skillKey -> {
            mcRPGPapiExpansion.registerPlaceholder(new SkillCurrentExperiencePlaceholder(skillKey));
        });
    }),
    SKILL_REMAINING_EXPERIENCE((mcRPG, mcRPGPapiExpansion) -> {
        mcRPG.getSkillRegistry().getRegisteredSkillKeys().forEach(skillKey -> {
            mcRPGPapiExpansion.registerPlaceholder(new SkillRemainingExperiencePlaceholder(skillKey));
        });
    }),
    ABILITY_TIER((mcRPG, mcRPGPapiExpansion) -> {
        AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();
        abilityRegistry.getAllAbilities().forEach(abilityKey -> {
            if (abilityRegistry.getRegisteredAbility(abilityKey).getApplicableAttributes().contains(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY)) {
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
