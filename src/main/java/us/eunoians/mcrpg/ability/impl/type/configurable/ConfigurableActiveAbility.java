package us.eunoians.mcrpg.ability.impl.type.configurable;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.type.ActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.CooldownableAbility;
import us.eunoians.mcrpg.ability.impl.type.ManaAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashSet;
import java.util.Set;

/**
 * This interface is used for all active abilities that are unlockable, have tiers,
 * and have cooldowns while pulling most of these values out of configuration files.
 * <p>
 * Please see any of the parent interfaces for more information about their specific
 * mechanisms.
 */
public interface ConfigurableActiveAbility extends CooldownableAbility, ConfigurableTierableAbility, ActiveAbility, ManaAbility {

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

    /**
     * Returns the mana cost required to activate this ability, evaluated from the
     * {@code mana-cost} formula in the ability's {@code tier-configuration.all-tiers} block.
     * <p>
     * The formula supports the {@code tier} variable (e.g., {@code "50-(7*tier)"}). The computed
     * value is clamped to the global minimum configured at
     * {@link MainConfigFile#MANA_MINIMUM_ABILITY_COST}. Implementations may override this
     * method to provide a fixed cost or a custom formula route.
     *
     * @param abilityHolder The {@link AbilityHolder} attempting to activate.
     * @return The mana cost (non-negative, at least the global minimum).
     */
    @Override
    default int getManaCost(@NotNull AbilityHolder abilityHolder) {
        YamlDocument yamlDocument = getYamlDocument();
        int tier = getCurrentAbilityTier(abilityHolder);
        Route tierSpecificRoute = Route.addTo(getRouteForTier(tier), "mana-cost");
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "mana-cost");
        String formula = yamlDocument.contains(tierSpecificRoute)
                ? yamlDocument.getString(tierSpecificRoute, "0")
                : yamlDocument.getString(allTiersRoute, "0");
        Parser parser = new Parser(formula);
        parser.setVariable("tier", tier);
        int computed = (int) parser.getValue();
        int globalMinimum = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG)
                .getInt(MainConfigFile.MANA_MINIMUM_ABILITY_COST, 5);
        return Math.max(computed, globalMinimum);
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
