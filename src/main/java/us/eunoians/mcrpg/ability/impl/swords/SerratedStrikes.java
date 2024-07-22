package us.eunoians.mcrpg.ability.impl.swords;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableActiveAbility;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.ability.ready.SwordReadyData;
import us.eunoians.mcrpg.api.event.ability.swords.SerratedStrikesActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Optional;
import java.util.Set;

/**
 * This ability activates by attacking an enemy after readying the user's sword. The ability
 * increases the activation rate of {@link Bleed} while active.
 */
public final class SerratedStrikes extends BaseAbility implements ConfigurableActiveAbility {

    public static final NamespacedKey SERRATED_STRIKES_KEY = new NamespacedKey(McRPG.getInstance(), "serrated_strikes");

    public SerratedStrikes() {
        super(SERRATED_STRIKES_KEY);
        addReadyingComponent(SwordsComponents.SWORDS_READY_COMPONENT, PlayerInteractEvent.class, 0);
        addReadyingComponent(SwordsComponents.SWORDS_READY_COMPONENT, PlayerInteractEntityEvent.class, 0);

        addActivatableComponent(SwordsComponents.HOLDING_SWORD_ACTIVATE_COMPONENT, EntityDamageByEntityEvent.class, 0);
        addActivatableComponent(SwordsComponents.SWORDS_ACTIVATE_ON_READY_COMPONENT, EntityDamageByEntityEvent.class, 1);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return McRPG.getInstance().getFileManager().getFile(FileType.SWORDS_CONFIG);
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(SwordsConfigFile.SERRATED_STRIKES_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }


    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("serrated_strikes");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Serrated Strikes";
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.STONE_SWORD);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
        LivingEntity entity = (LivingEntity) damageEvent.getEntity();
        SerratedStrikesActivateEvent serratedStrikesActivateEvent = new SerratedStrikesActivateEvent(abilityHolder, entity, getDuration(getCurrentAbilityTier(abilityHolder)));
        Bukkit.getPluginManager().callEvent(serratedStrikesActivateEvent);

        if (!serratedStrikesActivateEvent.isCancelled()) {
            abilityHolder.addActiveAbility(this, serratedStrikesActivateEvent.getDuration());
            putHolderOnCooldown(abilityHolder);
        }
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(SwordsConfigFile.SERRATED_STRIKES_ENABLED);
    }

    @NotNull
    @Override
    public Optional<ReadyData> getReadyData() {
        return Optional.of(new SwordReadyData());
    }

    /**
     * Gets the duration of this ability for the given tier.
     *
     * @param tier The tier to get the duration for.
     * @return The duration of this ability.
     */
    public int getDuration(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "duration");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "duration");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getInt(tierRoute);
        } else {
            return swordsConfig.getInt(allTiersRoute);
        }
    }

    /**
     * Gets the amount to boost {@link Bleed}'s activation chance by.
     *
     * @param tier The tier to get the activation chance boost for.
     * @return The amount to boost {@link Bleed}'s activation chance for.
     */
    public double getBoostToBleedActivation(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "bleed-activation-boost");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "bleed-activation-boost");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getDouble(tierRoute);
        } else {
            return swordsConfig.getDouble(allTiersRoute);
        }
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableActiveAbility.super.getApplicableAttributes();
    }
}
