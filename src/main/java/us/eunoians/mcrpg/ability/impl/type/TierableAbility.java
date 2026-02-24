package us.eunoians.mcrpg.ability.impl.type;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Optional;
import java.util.Set;

/**
 * This ability should be extended whenever an ability has tiers. All abilities
 * with tiers SHOUlD be {@link UnlockableAbility UnlockableAbilities}, thus why this provides
 * extended functionality.
 */
// TODO this shouldnt need to be skill/level based. Why not allow tier progression from other ways
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
     * @deprecated Upgrade points are being deprecated in favor of quest-based upgrades.
     *             Use {@link #getUpgradeQuestKey(int)} instead.
     */
    @Deprecated
    int getUpgradeCostForTier(int tier);

    /**
     * Gets the {@link NamespacedKey} of the quest definition that must be completed to
     * upgrade this ability to the given tier. Returns empty if no upgrade quest is
     * configured for this tier (e.g., tier 1 typically has no quest).
     *
     * @param tier the target tier
     * @return an {@link Optional} containing the quest definition key, or empty if not configured
     */
    @NotNull
    default Optional<NamespacedKey> getUpgradeQuestKey(int tier) {
        return Optional.empty();
    }

    @Override
    default int getUnlockLevel() {
        return getUnlockLevelForTier(1);
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE,
                AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY,
                AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
    }

    /**
     * Gets the current tier of this ability for the provided {@link AbilityHolder}.
     *
     * @param abilityHolder The {@link AbilityHolder} to get the current tier for.
     * @return The current tier of this ability for the provided {@link AbilityHolder}.
     */
    default int getCurrentAbilityTier(@NotNull AbilityHolder abilityHolder) {
        var abilityData = abilityHolder.getAbilityData(this);
        int currentTier = 0;
        if (abilityData.isPresent()) {
            var attributeData = abilityData.get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            if (attributeData.isPresent() && attributeData.get() instanceof AbilityTierAttribute attribute) {
                currentTier = attribute.getContent();
            }
        }
        return currentTier;
    }
}
