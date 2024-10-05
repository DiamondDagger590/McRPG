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
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.event.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.event.event.ability.swords.EnhancedBleedActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
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
    public Optional<String> getLegacyName() {
        return Optional.of("Bleed+");
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("enhanced_bleed");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Enhanced Bleed";
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        int currentTier = getCurrentAbilityTier(mcRPGPlayer.asSkillHolder());
        return List.of("<gray>Enhances Bleed ability to do more damage each tick, with a chance to deal bonus damage.",
                "<gray>Base Damage Boost: <gold>" + getBaseBleedDamageIncrease(currentTier),
                "<gray>Bonus Damage Chance: <gold>" + getActivationChance(currentTier),
                "<gray>Bonus Damage: <gold>" + getAdditionalBleedDamageBoost(currentTier));
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

        if (!enhancedBleedActivateEvent.isCancelled()) {
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
}
