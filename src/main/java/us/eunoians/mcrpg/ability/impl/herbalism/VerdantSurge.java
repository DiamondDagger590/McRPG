package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.ability.ready.HerbalismReadyData;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.herbalism.VerdantSurgeActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.task.ability.VerdantSurgePulseTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.COOLDOWN;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.PULSE_COUNT;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.RADIUS;

/**
 * Verdant Surge is a herbalism ability focused on allowing users to grow large areas of crops for harvesting.
 *
 * It functions by creating multiple {@link VerdantSurgePulseTask}s that emit waves of growth that spread away from the player,
 * growing any crops along the way.
 */
public class VerdantSurge extends McRPGAbility implements ConfigurableActiveAbility, ConfigurableSkillAbility {

    public static final NamespacedKey VERDANT_SURGE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "verdant_surge");

    public VerdantSurge(@NotNull McRPG mcRPG) {
        super(mcRPG, VERDANT_SURGE_KEY);
        addReadyingComponent(HerbalismComponents.HERBALISM_READY_COMPONENT, PlayerInteractEvent.class, 0);
        addReadyingComponent(HerbalismComponents.HERBALISM_READY_COMPONENT, PlayerInteractEntityEvent.class, 0);

        addActivatableComponent(HerbalismComponents.HERBALISM_ACTIVATE_ON_READY_COMPONENT, PlayerInteractEvent.class, 0);
        addActivatableComponent(HerbalismComponents.HOLDING_HOE_INTERACT_ACTIVATE_COMPONENT, PlayerInteractEvent.class, 1);
    }

    @NotNull
    @Override
    public Optional<ReadyData> getReadyData() {
        return Optional.of(new HerbalismReadyData());
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Herbalism.HERBALISM_KEY;
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return HerbalismConfigFile.VERDANT_SURGE_TIER_CONFIGURATION_HEADER;
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(HerbalismConfigFile.VERDANT_SURGE_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.HERBALISM_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.VERDANT_SURGE_DISPLAY_ITEM_HEADER;
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return HerbalismConfigFile.VERDANT_SURGE_ENABLED;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "verdant_surge";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
        Player player = playerInteractEvent.getPlayer();
        McRPGPlayer mcRPGPlayer = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId()).orElseThrow(IllegalStateException::new);
        int pulseCount = getPulseCount(getCurrentAbilityTier(abilityHolder));
        int pulseRadius = getRadius(getCurrentAbilityTier(abilityHolder));
        double delay = 0;

        abilityHolder.removeActiveAbility(this);
        VerdantSurgeActivateEvent verdantSurgeActivateEvent = new VerdantSurgeActivateEvent(abilityHolder, pulseCount, pulseRadius);
        Bukkit.getPluginManager().callEvent(verdantSurgeActivateEvent);
        if (verdantSurgeActivateEvent.isCancelled()) {
            return;
        }
        putHolderOnCooldown(abilityHolder);
        for (int i = 0; i < verdantSurgeActivateEvent.getPulseCount(); i++) {
            VerdantSurgePulseTask verdantSurgePulseTask = new VerdantSurgePulseTask(this.getPlugin(), mcRPGPlayer, delay, verdantSurgeActivateEvent.getMaxPulseRadius());
            verdantSurgePulseTask.runTask();
            delay += 1.5;
        }
    }

    /**
     * Gets the range of Verdant Surge pulses.
     *
     * @param tier The tier to get the range for.
     * @return The range of a Verdant Surge pulse for the provided tier.
     */
    public int getRadius(int tier) {
        YamlDocument herbalismConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "pulse-radius");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "pulse-radius");
        Parser parser;
        if (herbalismConfig.contains(tierRoute)) {
            parser = new Parser(herbalismConfig.getString(tierRoute));
        } else {
            parser = new Parser(herbalismConfig.getString(allTiersRoute));
        }
        parser.setVariable("tier", tier);
        return (int) parser.getValue();
    }

    /**
     * Gets the count of Verdant Surge pulses.
     *
     * @param tier The tier to get the range for.
     * @return The count of Verdant Surge pulses to be emitted for the provided tier.
     */
    public int getPulseCount(int tier) {
        YamlDocument herbalismConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "pulses");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "pulses");
        Parser parser;
        if (herbalismConfig.contains(tierRoute)) {
            parser = new Parser(herbalismConfig.getString(tierRoute));
        } else {
            parser = new Parser(herbalismConfig.getString(allTiersRoute));
        }
        parser.setVariable("tier", tier);
        return (int) parser.getValue();
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(RADIUS.getKey(), Integer.toString(getRadius(getCurrentAbilityTier(player.asSkillHolder()))));
        placeholders.put(COOLDOWN.getKey(), Long.toString(getCooldown(player.asSkillHolder())));
        placeholders.put(PULSE_COUNT.getKey(), Long.toString(getPulseCount(getCurrentAbilityTier(player.asSkillHolder()))));
        return placeholders;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableActiveAbility.super.getApplicableAttributes();
    }
}
