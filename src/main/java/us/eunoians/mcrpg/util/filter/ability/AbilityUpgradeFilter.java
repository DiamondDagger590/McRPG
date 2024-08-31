package us.eunoians.mcrpg.util.filter.ability;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.PlayerContextFilter;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.TierableAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Collection;

/**
 * This filter is used to filter a collection of {@link Ability Abilities} so only abilities that upgraded
 * for the given player are left.
 */
public class AbilityUpgradeFilter implements PlayerContextFilter<Ability> {

    @Override
    public Collection<Ability> filter(@NotNull CorePlayer corePlayer, @NotNull Collection<Ability> collection) {
        if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            collection = collection.stream()
                    .filter(ability -> ability instanceof TierableAbility)
                    .map(ability -> (TierableAbility) ability)
                    // Filter out unlocked abilities
                    .filter(tierableAbility -> {
                        var dataOptional = skillHolder.getAbilityData(tierableAbility);
                        if (dataOptional.isPresent()) {
                            var upgradableData = dataOptional.get().getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY);
                            // If it's an upgradable ability
                            if (upgradableData.isPresent()) {
                                // If it's also an unlockable ability, we only want to display it when it's unlocked
                                return tierableAbility.isAbilityUnlocked(skillHolder);
                            }
                        }
                        return false;
                    })
                    .filter(tierableAbility -> skillHolder.getAbilityData(tierableAbility).get().getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY).get() instanceof AbilityTierAttribute abilityTierAttribute
                            && tierableAbility.getMaxTier() > abilityTierAttribute.getContent())
                    .filter(tierableAbility -> {
                        // Validate they have enough upgrade points for this
                        int currentTier = (int) skillHolder.getAbilityData(tierableAbility).get().getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY).get().getContent();
                        return skillHolder.getUpgradePoints() >= tierableAbility.getUpgradeCostForTier(currentTier);
                    })
                    .filter(tierableAbility -> {
                        // If the ability has a skill, is it high enough level to upgrade it
                        if (tierableAbility.getSkill().isPresent()) {
                            var skillData = skillHolder.getSkillHolderData(tierableAbility.getSkill().get());
                            int currentTier = (int) skillHolder.getAbilityData(tierableAbility).get().getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY).get().getContent();
                            if (skillData.isPresent()) {
                                return skillData.get().getCurrentLevel() >= tierableAbility.getUnlockLevelForTier(currentTier);
                            }
                        }
                        return true;
                    })
                    .map(tierableAbility -> (Ability) tierableAbility)
                    .toList();
        }
        return collection;
    }
}
