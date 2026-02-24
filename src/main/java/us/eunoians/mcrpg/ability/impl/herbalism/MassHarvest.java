package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.collection.ReloadableSet;
import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
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
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.ReadyAbility;
import us.eunoians.mcrpg.ability.impl.type.ReloadableContentAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.ability.ready.HerbalismReadyData;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.herbalism.MassHarvestActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.task.ability.herbalism.MassHarvestPulseTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.COOLDOWN;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.RADIUS;

/**
 * This ability allows players to harvest blocks in a radius around them.
 */
public final class MassHarvest extends McRPGAbility implements ConfigurableActiveAbility, ConfigurableSkillAbility,
        ReloadableContentAbility, ReadyAbility {

    public static final NamespacedKey MASS_HARVEST_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mass_harvest");
    private final ReloadableSet<CustomBlockWrapper> VALID_BLOCK_TYPES;

    public MassHarvest(@NotNull McRPG mcRPG) {
        super(mcRPG, MASS_HARVEST_KEY);
        this.VALID_BLOCK_TYPES = new ReloadableSet<>(getYamlDocument(), HerbalismConfigFile.MASS_HARVEST_VALID_BLOCKS,
                strings -> strings.stream().map(CustomBlockWrapper::new).collect(Collectors.toSet()));
        addReadyingComponent(HerbalismComponents.HERBALISM_READY_COMPONENT, PlayerInteractEvent.class, 0);
        addReadyingComponent(HerbalismComponents.HERBALISM_READY_COMPONENT, PlayerInteractEntityEvent.class, 0);

        addActivatableComponent(HerbalismComponents.HERBALISM_ACTIVATE_ON_READY_COMPONENT, PlayerInteractEvent.class, 0);
        addActivatableComponent(HerbalismComponents.HOLDING_HOE_INTERACT_ACTIVATE_COMPONENT, PlayerInteractEvent.class, 1);
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Herbalism.HERBALISM_KEY;
    }

    @NotNull
    @Override
    public HerbalismReadyData getReadyData() {
        return new HerbalismReadyData();
    }

    /**
     * Get a {@link ReloadableSet} of {@link CustomBlockWrapper}s that can trigger this ability.
     *
     * @return A {@link ReloadableSet} of {@link CustomBlockWrapper}s that can trigger this ability.
     */
    public ReloadableSet<CustomBlockWrapper> getValidBlockTypes() {
        return VALID_BLOCK_TYPES;
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return HerbalismConfigFile.MASS_HARVEST_TIER_CONFIGURATION_HEADER;
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(HerbalismConfigFile.MASS_HARVEST_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.HERBALISM_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.MASS_HARVEST_DISPLAY_ITEM_HEADER;
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return HerbalismConfigFile.MASS_HARVEST_ENABLED;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "mass_harvest";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
        Player player = playerInteractEvent.getPlayer();
        McRPGPlayer mcRPGPlayer = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId()).orElseThrow(IllegalStateException::new);
        int pulseRadius = getRadius(getCurrentAbilityTier(abilityHolder));
        abilityHolder.unreadyHolder();

        MassHarvestActivateEvent massHarvestActivateEvent = new MassHarvestActivateEvent(abilityHolder, pulseRadius);
        Bukkit.getPluginManager().callEvent(massHarvestActivateEvent);
        if (massHarvestActivateEvent.isCancelled()) {
            return;
        }
        abilityHolder.addActiveAbility(this);
        putHolderOnCooldown(abilityHolder);
        MassHarvestPulseTask massHarvestPulseTask = new MassHarvestPulseTask(this.getPlugin(), mcRPGPlayer, this, massHarvestActivateEvent.getMaxPulseRadius());
        massHarvestPulseTask.runTask();
        abilityHolder.removeActiveAbility(this);
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(VALID_BLOCK_TYPES);
    }

    /**
     * Gets the range of Mass Harvest pulses.
     *
     * @param tier The tier to get the range for.
     * @return The range of a Mass Harvest pulse for the provided tier.
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

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(RADIUS.getKey(), Integer.toString(getRadius(getCurrentAbilityTier(player.asSkillHolder()))));
        placeholders.put(COOLDOWN.getKey(), Long.toString(getCooldown(player.asSkillHolder())));
        return placeholders;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        Set<NamespacedKey> applicableAttributes = new HashSet<>(ConfigurableActiveAbility.super.getApplicableAttributes());
        applicableAttributes.add(AbilityAttributeRegistry.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE);
        return applicableAttributes;
    }
}
