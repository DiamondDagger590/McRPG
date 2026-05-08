package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import com.diamonddagger590.mccore.parser.Parser;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ParserConfigKeys;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.event.ability.swords.DeeperWoundActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This ability is an unlockable ability for {@link Swords} that
 * can increase the duration of the {@link Bleed} ability
 */
@ParserConfigKeys({"deeper-wound-activation-chance", "deeper-wound-cycle-increase"})
public final class DeeperWound extends McRPGAbility implements ConfigurableTierableAbility, PassiveAbility, ConfigurableSkillAbility {

    public static final NamespacedKey DEEPER_WOUND_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "deeper_wound");

    public DeeperWound(@NotNull McRPG plugin) {
        super(plugin, DEEPER_WOUND_KEY);
        addActivatableComponent(DeeperWoundComponents.DEEPER_WOUND_ACTIVATE_COMPONENT, BleedActivateEvent.class, 0);
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Swords.SWORDS_KEY;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "deeper_wound";
    }

    @Override
    public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BleedActivateEvent bleedActivateEvent = (BleedActivateEvent) event;
        DeeperWoundActivateEvent deeperWoundActivateEvent = new DeeperWoundActivateEvent(abilityHolder, bleedActivateEvent.getBleedingEntity(), getAdditionalBleedCycles(getCurrentAbilityTier(abilityHolder)));
        Bukkit.getPluginManager().callEvent(deeperWoundActivateEvent);

        if (deeperWoundActivateEvent.isCancelled()) {
            return false;
        }
        bleedActivateEvent.setBleedCycles(bleedActivateEvent.getBleedCycles() + deeperWoundActivateEvent.getAdditionalBleedCycles());
        return true;
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(SwordsConfigFile.DEEPER_WOUND_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.DEEPER_WOUND_DISPLAY_ITEM_HEADER;
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return SwordsConfigFile.DEEPER_WOUND_ENABLED;
    }

    /**
     * Gets the chance of activating this ability for the given tier.
     *
     * @param tier The tier to get the activation chance for
     * @return The activation chance for this ability.
     */
    public double getActivationChance(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "deeper-wound-activation-chance");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "deeper-wound-activation-chance");
        Parser parser;
        if (swordsConfig.contains(tierRoute)) {
            parser = new Parser(swordsConfig.getString(tierRoute));
        } else {
            parser = new Parser(swordsConfig.getString(allTiersRoute));
        }
        parser.setVariable("tier", tier);
        return parser.getValue();
    }

    /**
     * Gets how many extra bleed cycles should be added if this ability activates.
     *
     * @param tier The tier to get the extra cycles for
     * @return The amount of extra bleed cycles
     */
    public int getAdditionalBleedCycles(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "deeper-wound-cycle-increase");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "deeper-wound-cycle-increase");
        Parser parser;
        if (swordsConfig.contains(tierRoute)) {
            parser = new Parser(swordsConfig.getString(tierRoute));
        } else {
            parser = new Parser(swordsConfig.getString(allTiersRoute));
        }
        parser.setVariable("tier", tier);
        return (int) parser.getValue();
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableTierableAbility.super.getApplicableAttributes();
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        int tier = getCurrentAbilityTier(player.asSkillHolder());
        placeholders.put(AbilityItemPlaceholderKeys.ACTIVATION_CHANCE.getKey(),
                McRPGMethods.getChanceNumberFormat().format(getActivationChance(tier)));
        placeholders.put(AbilityItemPlaceholderKeys.ADDITIONAL_BLEED_CYCLES.getKey(),
                Integer.toString(getAdditionalBleedCycles(tier)));
        return placeholders;
    }
}
