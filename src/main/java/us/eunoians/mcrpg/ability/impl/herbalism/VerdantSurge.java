package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.herbalism.VerdantSurgeActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.task.ability.herbalism.VerdantSurgePulseTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.COOLDOWN;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.MANA_COST;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.PULSE_COUNT;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.RADIUS;

/**
 * Verdant Surge is a herbalism ability focused on allowing users to grow large areas of crops for harvesting.
 * <p>
 * It functions by creating multiple {@link VerdantSurgePulseTask}s that emit waves of growth that spread away from the player,
 * growing any crops along the way.
 */
public final class VerdantSurge extends McRPGAbility implements ConfigurableActiveAbility,
        ConfigurableSkillAbility, ComboActivatable {

    public static final NamespacedKey VERDANT_SURGE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "verdant_surge");

    public VerdantSurge(@NotNull McRPG mcRPG) {
        super(mcRPG, VERDANT_SURGE_KEY);
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

    /**
     * Delegates to {@link #comboActivate(AbilityHolder)}. This ability has no activation
     * components registered, so this method is never called via the event-listener path;
     * it remains implemented to satisfy the {@link us.eunoians.mcrpg.ability.Ability} interface.
     *
     * @param abilityHolder The holder activating this ability.
     * @param event         The triggering event (unused).
     * @return The result of {@link #comboActivate(AbilityHolder)}.
     */
    @Override
    public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        return comboActivate(abilityHolder);
    }

    /**
     * Activates Verdant Surge via the combo system. Resolves the
     * {@link McRPGPlayer}, fires {@link VerdantSurgeActivateEvent},
     * and schedules pulse tasks if the event is not cancelled.
     *
     * @param abilityHolder The holder activating this ability.
     * @return {@code true} if the ability executed, {@code false} if the
     *         player could not be resolved or the event was cancelled.
     */
    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        var playerOpt = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(abilityHolder.getUUID());
        if (playerOpt.isEmpty()) {
            return false;
        }
        return performVerdantSurge(abilityHolder, playerOpt.get());
    }

    /**
     * Executes the core Verdant Surge effect — firing the activate event
     * and scheduling pulse tasks.
     *
     * @param abilityHolder The {@link AbilityHolder} activating the ability.
     * @param mcRPGPlayer   The {@link McRPGPlayer} associated with the holder.
     * @return {@code true} if the surge was started successfully (event not cancelled).
     */
    private boolean performVerdantSurge(@NotNull AbilityHolder abilityHolder, @NotNull McRPGPlayer mcRPGPlayer) {
        int pulseCount = getPulseCount(getCurrentAbilityTier(abilityHolder));
        double pulseRadius = getRadius(getCurrentAbilityTier(abilityHolder));

        VerdantSurgeActivateEvent verdantSurgeActivateEvent = new VerdantSurgeActivateEvent(abilityHolder, pulseCount, pulseRadius);
        Bukkit.getPluginManager().callEvent(verdantSurgeActivateEvent);
        if (verdantSurgeActivateEvent.isCancelled()) {
            return false;
        }
        abilityHolder.addActiveAbility(this);
        double delay = 0;
        for (int i = 0; i < verdantSurgeActivateEvent.getPulseCount(); i++) {
            VerdantSurgePulseTask verdantSurgePulseTask = new VerdantSurgePulseTask(this.getPlugin(), mcRPGPlayer, delay, verdantSurgeActivateEvent.getMaxPulseRadius());
            verdantSurgePulseTask.runTask();
            delay += 1.5;
        }
        abilityHolder.removeActiveAbility(this);
        return true;
    }

    /**
     * Gets the range of Verdant Surge pulses.
     *
     * @param tier The tier to get the range for.
     * @return The range of a Verdant Surge pulse for the provided tier.
     */
    public double getRadius(int tier) {
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
        return parser.getValue();
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
        placeholders.put(RADIUS.getKey(), Double.toString(getRadius(getCurrentAbilityTier(player.asSkillHolder()))));
        placeholders.put(COOLDOWN.getKey(), Long.toString(getCooldown(player.asSkillHolder())));
        placeholders.put(PULSE_COUNT.getKey(), Long.toString(getPulseCount(getCurrentAbilityTier(player.asSkillHolder()))));
        placeholders.put(MANA_COST.getKey(), Integer.toString(getManaCost(player.asSkillHolder())));
        return placeholders;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableActiveAbility.super.getApplicableAttributes();
    }
}
