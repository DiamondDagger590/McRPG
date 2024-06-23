package us.eunoians.mcrpg.ability.impl.swords;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.EnhancedBleedActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Optional;
import java.util.Random;

/**
 * This ability is an unlockable ability for {@link Swords} that
 * can increase the damage per tick for the {@link Bleed} ability
 */
public final class EnhancedBleed extends BaseAbility implements ConfigurableTierableAbility, PassiveAbility {

    public static final NamespacedKey ENHANCED_BLEED_KEY = new NamespacedKey(McRPG.getInstance(), "enhanced_bleed");
    private static final Random RANDOM = new Random();


    public EnhancedBleed() {
        super(ENHANCED_BLEED_KEY);
        addActivatableComponent(EnhancedBleedComponents.ENHANCED_BLEED_ACTIVATE_COMPONENT, BleedActivateEvent.class, 0);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getLegacyName() {
        return Optional.of("Bleed+");
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Enhanced Bleed";
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.SPIDER_EYE);
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

        if(!enhancedBleedActivateEvent.isCancelled()) {
            bleedActivateEvent.setBleedDamage(bleedActivateEvent.getBleedDamage() + enhancedBleedActivateEvent.getAdditionalBleedDamage());
        }
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return McRPG.getInstance().getFileManager().getFile(FileType.SWORDS_CONFIG);
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
        }
        else {
            return swordsConfig.getInt(allTiersRoute);
        }
    }

    public double getActivationChance(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "enhanced-bleed-activation-chance");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "enhanced-bleed-activation-chance");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getDouble(tierRoute);
        }
        else {
            return swordsConfig.getDouble(allTiersRoute);
        }
    }

    public int getAdditionalBleedDamageBoost(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "enhanced-bleed-damage-boost");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "enhanced-bleed-damage-boost");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getInt(tierRoute);
        }
        else {
            return swordsConfig.getInt(allTiersRoute);
        }
    }
}
