package us.eunoians.mcrpg.ability;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.quest.Quest;

import java.util.Set;

/**
 * This ability should be extended whenever an ability has tiers. All abilities
 * with tiers SHOUlD be {@link UnlockableAbility UnlockableAbilities}, thus why this provides
 * extended functionality.
 */
public abstract class TierableAbility extends UnlockableAbility {

    public TierableAbility(@NotNull NamespacedKey abilityKey) {
        super(abilityKey);
    }

    /**
     * Get the maximum tier that this ability can be upgraded to.
     *
     * @return The maximum tier that this ability can be upgraded to.
     */
    public abstract int getMaxTier();

    /**
     * Gets the level required for this ability to be upgraded to the provided tier.
     *
     * @param tier The tier to get the level requirement of
     * @return The level required for this ability to be upgraded to the provided tier.
     */
    public abstract int getUnlockLevelForTier(int tier);

    /**
     * Gets the ability point cost for this ability to be upgraded to the provided tier.
     *
     * @param tier The tier to get the ability point cost of
     * @return The ability point cost for this ability to be upgraded to the provided tier.
     */
    public abstract int getUpgradeCostForTier(int tier);

    public abstract Quest getUpgradeQuestForTier(int tier);

    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE,
                AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY,
                AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE);
    }
}
