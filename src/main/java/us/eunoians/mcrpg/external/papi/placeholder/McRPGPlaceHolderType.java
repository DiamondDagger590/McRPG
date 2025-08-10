package us.eunoians.mcrpg.external.papi.placeholder;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.external.papi.McRPGPapiExpansion;
import us.eunoians.mcrpg.external.papi.placeholder.ability.AbilityTierPlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.experience.BoostedExperiencePlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.experience.RedeemableExperiencePlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.experience.RedeemableLevelsPlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.experience.RestedExperiencePlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.skill.SkillCurrentExperiencePlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.skill.SkillCurrentLevelPlaceholder;
import us.eunoians.mcrpg.external.papi.placeholder.skill.SkillRemainingExperiencePlaceholder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This enum is responsible for registering PAPI placeholders
 * as a single {@link McRPGPlaceholder} might be designed to be generic enough
 * to support multiple registrations (such as {@link SkillCurrentLevelPlaceholder} being
 * registered one per skill).
 */
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
    BOOSTED_EXPERIENCE((mcRPG, mcRPGPapiExpansion) -> {
        mcRPGPapiExpansion.registerPlaceholder(new BoostedExperiencePlaceholder());
    }),
    REDEEMABLE_EXPERIENCE((mcRPG, mcRPGPapiExpansion) -> {
        mcRPGPapiExpansion.registerPlaceholder(new RedeemableExperiencePlaceholder());
    }),
    REDEEMABLE_LEVELS((mcRPG, mcRPGPapiExpansion) -> {
        mcRPGPapiExpansion.registerPlaceholder(new RedeemableLevelsPlaceholder());
    }),
    RESTED_EXPERIENCE((mcRPG, mcRPGPapiExpansion) -> {
        mcRPGPapiExpansion.registerPlaceholder(new RestedExperiencePlaceholder());
    }),
    ;

    private final PlaceholderRegisterFunction placeholderRegisterFunction;

    McRPGPlaceHolderType(@NotNull PlaceholderRegisterFunction placeholderRegisterFunction) {
        this.placeholderRegisterFunction = placeholderRegisterFunction;
    }

    /**
     * Registers the placeholders this enum value is in charge of registering.
     *
     * @param mcRPG              The plugin instance to use for registering.
     * @param mcRPGPapiExpansion The expansion being used for registration.
     */
    public void registerPlaceholders(@NotNull McRPG mcRPG, @NotNull McRPGPapiExpansion mcRPGPapiExpansion) {
        placeholderRegisterFunction.registerPlaceholders(mcRPG, mcRPGPapiExpansion);
    }
}
