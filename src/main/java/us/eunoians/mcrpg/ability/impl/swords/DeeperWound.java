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
import us.eunoians.mcrpg.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.event.ability.swords.DeeperWoundActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
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
    public Optional<String> getLegacyName() {
        return Optional.of("Deeper Wound");
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("deeper_wound");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Deeper Wound";
    }

    @Override
    @NotNull
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        int currentTier = getCurrentAbilityTier(mcRPGPlayer.asSkillHolder());
        return List.of("<gray>Enhances Bleed ability to last longer.",
                "<gray>Activation Chance: <gold>" + getActivationChance(currentTier),
                "<gray>Extra Bleed Ticks: <gold>" + getAdditionalBleedCycles(currentTier));
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.RED_DYE);
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
        return getPlugin().getFileManager().getFile(FileType.SWORDS_CONFIG);
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
