package us.eunoians.mcrpg.ability.impl;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.quest.Quest;

import java.util.Set;

/**
 * This ability should be extended whenever an ability has tiers. All abilities
 * with tiers SHOUlD be {@link UnlockableAbility UnlockableAbilities}, thus why this provides
 * extended functionality.
 */
public interface TierableAbility extends UnlockableAbility {

    /**
     * Get the maximum tier that this ability can be upgraded to.
     *
     * @return The maximum tier that this ability can be upgraded to.
     */
    int getMaxTier();

    /**
     * Gets the level required for this ability to be upgraded to the provided tier.
     *
     * @param tier The tier to get the level requirement of
     * @return The level required for this ability to be upgraded to the provided tier.
     */
    int getUnlockLevelForTier(int tier);

    /**
     * Gets the ability point cost for this ability to be upgraded to the provided tier.
     *
     * @param tier The tier to get the ability point cost of
     * @return The ability point cost for this ability to be upgraded to the provided tier.
     */
    int getUpgradeCostForTier(int tier);

    @Override
    default int getUnlockLevel() {
        return getUnlockLevelForTier(1);
    }

    /**
     * Gets the {@link Quest} needed to upgrade this ability at the provided tier.
     * @param tier The tier to get the upgrade {@link Quest} for
     * @return The {@link Quest} needed to upgrade this ability.
     */
    @NotNull
    Quest getUpgradeQuestForTier(int tier);

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE,
                AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY,
                AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE);
    }

    /**
     * Gets the current tier of this ability for the provided {@link AbilityHolder}.,
     * @param abilityHolder The {@link AbilityHolder} to get the current tier for.
     * @return The current tier of this ability for the provided {@link AbilityHolder}.
     */
    default int getCurrentAbilityTier(@NotNull AbilityHolder abilityHolder) {
        var abilityData = abilityHolder.getAbilityData(this);
        int currentTier = 0;
        if (abilityData.isPresent()) {
            var attributeData = abilityData.get().getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY);
            if (attributeData.isPresent() && attributeData.get() instanceof AbilityTierAttribute attribute) {
                currentTier = attribute.getContent();
            }
        }
        return currentTier;
    }
}
