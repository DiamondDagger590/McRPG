package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.builder.item.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.event.ability.swords.EnhancedBleedActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * This ability is an unlockable ability for {@link Swords} that
 * can increase the damage per tick for the {@link Bleed} ability
 */
public final class EnhancedBleed extends McRPGAbility implements ConfigurableTierableAbility, PassiveAbility {

    public static final NamespacedKey ENHANCED_BLEED_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "enhanced_bleed");
    private static final Random RANDOM = new Random();

    public EnhancedBleed(@NotNull McRPG mcRPG) {
        super(mcRPG, ENHANCED_BLEED_KEY);
        addActivatableComponent(EnhancedBleedComponents.ENHANCED_BLEED_ACTIVATE_COMPONENT, BleedActivateEvent.class, 0);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "enhanced_bleed";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BleedActivateEvent bleedActivateEvent = (BleedActivateEvent) event;
        int currentTier = getCurrentAbilityTier(abilityHolder);
        int baseDamageIncrease = getBaseBleedDamageIncrease(currentTier);
        double bonusDamageActivationChance = getActivationChance(currentTier);
        int bonusDamage = getAdditionalBleedDamageBoost(currentTier);
        // Enhanced Bleed always activates, but the additional bonus damage doesnt always activate
        int totalDamage = baseDamageIncrease + (bonusDamageActivationChance * 1000 > RANDOM.nextInt(100000) ? bonusDamage : 0);
        EnhancedBleedActivateEvent enhancedBleedActivateEvent = new EnhancedBleedActivateEvent(abilityHolder, bleedActivateEvent.getBleedingEntity(), totalDamage);
        Bukkit.getPluginManager().callEvent(enhancedBleedActivateEvent);

        if (!enhancedBleedActivateEvent.isCancelled()) {
            bleedActivateEvent.setBleedDamage(bleedActivateEvent.getBleedDamage() + enhancedBleedActivateEvent.getAdditionalBleedDamage());
        }
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.ENHANCED_BLEED_DISPLAY_ITEM_HEADER;
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER;
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(SwordsConfigFile.ENHANCED_BLEED_AMOUNT_OF_TIERS);
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(SwordsConfigFile.ENHANCED_BLEED_ENABLED);
    }

    public int getBaseBleedDamageIncrease(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "enhanced-bleed-base-damage-increase");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "enhanced-bleed-base-damage-increase");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getInt(tierRoute);
        } else {
            return swordsConfig.getInt(allTiersRoute);
        }
    }

    /**
     * Gets the chance of activating this ability for the given tier.
     *
     * @param tier The tier to get the activation chance for
     * @return The activation chance for this ability.
     */
    public double getActivationChance(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "enhanced-bleed-activation-chance");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "enhanced-bleed-activation-chance");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getDouble(tierRoute);
        } else {
            return swordsConfig.getDouble(allTiersRoute);
        }
    }

    /**
     * Gets the additional amount of damage bleed should do if this ability activates.
     *
     * @param tier The tier to get the additional damage for.
     * @return The amount of additional damage to add to bleed.
     */
    public int getAdditionalBleedDamageBoost(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "enhanced-bleed-damage-boost");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "enhanced-bleed-damage-boost");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getInt(tierRoute);
        } else {
            return swordsConfig.getInt(allTiersRoute);
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
        placeholders.put(AbilityItemPlaceholderKeys.BASE_DAMAGE_BOOST.getKey(),
                Integer.toString(getBaseBleedDamageIncrease(tier)));
        placeholders.put(AbilityItemPlaceholderKeys.BONUS_DAMAGE_CHANCE.getKey(),
                McRPGMethods.getChanceNumberFormat().format(getActivationChance(tier)));
        placeholders.put(AbilityItemPlaceholderKeys.BONUS_DAMAGE.getKey(),
                Integer.toString(getAdditionalBleedDamageBoost(tier)));
        return placeholders;
    }
}
