package us.eunoians.mcrpg.ability.impl.type.configurable;

import com.diamonddagger590.mccore.parser.Parser;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This interface represents an {@link Ability} that has the same behavior as a {@link TierableAbility}, except
 * the values are mostly pulled out of a configuration provided by {@link ConfigurableAbility}.
 * <p>
 * This class assumes that all ability configurations follow the same essential yaml format where
 * there is tier specific configuration, but if a value isn't found there then it should be looked for
 * in the 'all-tiers' configuration section.
 */
public interface ConfigurableTierableAbility extends ConfigurableAbility, TierableAbility {

    /** Tracks which abilities have logged an inference warning already. */
    Set<NamespacedKey> INFERRED_UPGRADE_QUEST_WARNED = ConcurrentHashMap.newKeySet();

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
        YamlDocument yamlDocument = getYamlDocument();
        Route tierRoute = Route.addTo(getRouteForTier(tier), "unlock-level");
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "unlock-level");
        Parser parser;
        if (yamlDocument.contains(tierRoute)) {
            parser = new Parser(yamlDocument.getString(tierRoute));
        } else {
            parser = new Parser(yamlDocument.getString(allTiersRoute));
        }
        parser.setVariable("tier", tier);
        return (int) parser.getValue();
    }

    @Deprecated
    @Override
    default int getUpgradeCostForTier(int tier) {
        YamlDocument yamlDocument = getYamlDocument();
        Route tierRoute = Route.addTo(getRouteForTier(tier), "upgrade-point-cost");
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "upgrade-point-cost");
        Parser parser;
        if (yamlDocument.contains(tierRoute)) {
            parser = new Parser(yamlDocument.getString(tierRoute));
        } else {
            parser = new Parser(yamlDocument.getString(allTiersRoute));
        }
        parser.setVariable("tier", tier);
        return (int) parser.getValue();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation reads from the YAML config, checking for a tier-specific
     * {@code upgrade-quest} key first (e.g., {@code tier-2.upgrade-quest}), then falling
     * back to {@code all-tiers.upgrade-quest}. The value should be a
     * {@link NamespacedKey} string (e.g., {@code "mcrpg:enhanced_bleed_t2"}).
     */
    @NotNull
    @Override
    default Optional<NamespacedKey> getUpgradeQuestKey(int tier) {
        YamlDocument yamlDocument = getYamlDocument();
        Route tierRoute = Route.addTo(getRouteForTier(tier), "upgrade-quest");
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "upgrade-quest");

        String questKeyStr = null;
        if (yamlDocument.contains(tierRoute)) {
            questKeyStr = yamlDocument.getString(tierRoute);
        } else if (yamlDocument.contains(allTiersRoute)) {
            questKeyStr = yamlDocument.getString(allTiersRoute);
        }

        if (questKeyStr != null && !questKeyStr.isEmpty()) {
            questKeyStr = questKeyStr.replace("{tier}", String.valueOf(tier));
            return Optional.ofNullable(NamespacedKey.fromString(questKeyStr));
        }

        NamespacedKey abilityKey = getAbilityKey();
        NamespacedKey inferred = new NamespacedKey(abilityKey.getNamespace(), abilityKey.getKey() + "_upgrade");
        if (INFERRED_UPGRADE_QUEST_WARNED.add(abilityKey)) {
            McRPG.getInstance().getLogger().warning(
                    "[TierableAbility] No upgrade-quest configured for " + abilityKey
                            + " (tier " + tier + "). Inferring generic upgrade quest key: " + inferred);
        }
        return Optional.of(inferred);
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE,
                AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY,
                AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
    }
}
