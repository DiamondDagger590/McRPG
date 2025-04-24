package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.builder.item.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKeys;
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
import java.util.Optional;
import java.util.Set;

/**
 * This ability is an unlockable ability for {@link Swords} that
 * can increase the duration of the {@link Bleed} ability
 */
public final class DeeperWound extends McRPGAbility implements ConfigurableTierableAbility, PassiveAbility {

    public static final NamespacedKey DEEPER_WOUND_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "deeper_wound");

    public DeeperWound(@NotNull McRPG plugin) {
        super(plugin, DEEPER_WOUND_KEY);
        addActivatableComponent(DeeperWoundComponents.DEEPER_WOUND_ACTIVATE_COMPONENT, BleedActivateEvent.class, 0);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "deeper_wound";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BleedActivateEvent bleedActivateEvent = (BleedActivateEvent) event;
        DeeperWoundActivateEvent deeperWoundActivateEvent = new DeeperWoundActivateEvent(abilityHolder, bleedActivateEvent.getBleedingEntity(), getAdditionalBleedCycles(getCurrentAbilityTier(abilityHolder)));
        Bukkit.getPluginManager().callEvent(deeperWoundActivateEvent);

        if (!deeperWoundActivateEvent.isCancelled()) {
            bleedActivateEvent.setBleedCycles(bleedActivateEvent.getBleedCycles() + deeperWoundActivateEvent.getAdditionalBleedCycles());
        }
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
        return LocalizationKeys.DEEPER_WOUND_DISPLAY_ITEM_HEADER;
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER;
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(SwordsConfigFile.DEEPER_WOUND_ENABLED);
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
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getDouble(tierRoute);
        } else {
            return swordsConfig.getDouble(allTiersRoute);
        }
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
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getInt(tierRoute, 0);
        } else {
            return swordsConfig.getInt(allTiersRoute, 0);
        }
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
        placeholders.put(AbilityItemPlaceholderKeys.ACTIVATION_CHANCE.getKey(), McRPGMethods.getChanceNumberFormat().format(getActivationChance(tier)));
        placeholders.put(AbilityItemPlaceholderKeys.ADDITIONAL_BLEED_CYCLES.getKey(), Integer.toString(getAdditionalBleedCycles(tier)));
        return placeholders;
    }
}
