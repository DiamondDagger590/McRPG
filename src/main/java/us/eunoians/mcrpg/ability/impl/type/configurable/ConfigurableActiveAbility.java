package us.eunoians.mcrpg.ability.impl.type.configurable;

import com.diamonddagger590.mccore.parser.Parser;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.type.ActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.CooldownableAbility;
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
        Parser parser;
        if (yamlDocument.contains(tierRoute)) {
            parser = new Parser(yamlDocument.getString(tierRoute));
        } else {
            parser = new Parser(yamlDocument.getString(allTiersRoute));
        }
        parser.setVariable("tier", tier);
        return (long) parser.getValue();
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
