package us.eunoians.mcrpg.ability.impl;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.HashSet;
import java.util.Set;

/**
 * This interface is used for all active abilities that are unlockable, have tiers,
 * and have cooldowns while pulling most of these values out of configuration files.
 * <p>
 * Please see any of the parent interfaces for more information about their specific
 * mechanisms.
 */
public interface ConfigurableActiveAbility extends CooldownableAbility, ConfigurableTierableAbility, ActiveAbility {

    @Override
    default long getCooldown(@NotNull AbilityHolder abilityHolder) {
        YamlDocument yamlDocument = getYamlDocument();
        int tier = getCurrentAbilityTier(abilityHolder);
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "cooldown");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "cooldown");
        if (yamlDocument.contains(tierRoute)) {
            return yamlDocument.getLong(tierRoute);
        } else {
            return yamlDocument.getLong(allTiersRoute);
        }
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        Set<NamespacedKey> attributes = new HashSet<>();
        attributes.addAll(CooldownableAbility.super.getApplicableAttributes());
        attributes.addAll(ConfigurableTierableAbility.super.getApplicableAttributes());
        return attributes;
    }
}
