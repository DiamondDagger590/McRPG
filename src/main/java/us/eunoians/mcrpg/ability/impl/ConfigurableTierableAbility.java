package us.eunoians.mcrpg.ability.impl;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.UpgradeQuestReward;
import us.eunoians.mcrpg.quest.objective.EntitySlayQuestObjective;

import java.util.Set;

/**
 * This interface represents an {@link Ability} that has the same behavior as a {@link TierableAbility}, except
 * the values are mostly pulled out of a configuration provided by {@link ConfigurableAbility}.
 * <p>
 * This class assumes that all ability configurations follow the same essential yaml format where
 * there is tier specific configuration, but if a value isn't found there then it should be looked for
 * in the 'all-tiers' configuration section.
 */
public interface ConfigurableTierableAbility extends ConfigurableAbility, TierableAbility {

    /**
     * Gets the {@link Route} that provides the tier configuration section for this ability.
     *
     * @return The {@link Route} that provides the tier configuration section for this ability/
     */
    @NotNull
    Route getAbilityTierConfigurationRoute();

    /**
     * Gets the {@link Route} for the tier configuration for the provided tier.
     *
     * @param tier The tier to get the {@link Route} fore.
     * @return The {@link Route} for the tier configuration for the provided tier.
     */
    @NotNull
    default Route getRouteForTier(int tier) {
        return Route.addTo(getAbilityTierConfigurationRoute(), "tier-" + tier);
    }

    /**
     * Gets the {@link Route} for the 'all-tiers' configuration section.
     *
     * @return The {@link Route} for the 'all-tiers' configuration section.
     */
    @NotNull
    default Route getRouteForAllTiers() {
        return Route.addTo(getAbilityTierConfigurationRoute(), "all-tiers");
    }

    @Override
    default int getUnlockLevelForTier(int tier) {
        return getYamlDocument().getInt(Route.addTo(getRouteForTier(tier), "unlock-level"));
    }

    @Override
    default int getUpgradeCostForTier(int tier) {
        YamlDocument yamlDocument = getYamlDocument();
        Route tierRoute = Route.addTo(getRouteForTier(tier), "upgrade-point-cost");
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "upgrade-point-cost");
        if (yamlDocument.contains(tierRoute)) {
            return yamlDocument.getInt(tierRoute);
        } else {
            return yamlDocument.getInt(allTiersRoute);
        }
    }

    @NotNull
    @Override
    default Quest getUpgradeQuestForTier(int tier) {
        // TODO go back and finish these
        Quest quest = new Quest(getAbilityKey().getKey());
        quest.addQuestReward(new UpgradeQuestReward());
        EntitySlayQuestObjective objective = new EntitySlayQuestObjective(quest, 10 * tier);
        quest.addQuestObjective(objective);
        return quest;
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE,
                AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY,
                AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE);
    }
}
