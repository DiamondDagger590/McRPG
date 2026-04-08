package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.ReadyAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.configuration.file.combo.ComboConfigFile;
import us.eunoians.mcrpg.ability.ready.SwordReadyData;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.swords.RageSpikeActivateEvent;
import us.eunoians.mcrpg.event.ability.swords.RageSpikeDamageEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rage Spike is an active ability that activates after the user readies their
 * sword and then crouches, blasting them forward and knocking back enemies and doing damage.
 */
public final class RageSpike extends McRPGAbility implements ConfigurableActiveAbility, ConfigurableSkillAbility, ReadyAbility, ComboActivatable {

    public static final NamespacedKey RAGE_SPIKE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "rage_spike");

    public RageSpike(@NotNull McRPG mcRPG) {
        super(mcRPG, RAGE_SPIKE_KEY);
        addReadyingComponent(SwordsComponents.SWORDS_READY_COMPONENT, PlayerInteractEvent.class, 0);
        addReadyingComponent(SwordsComponents.SWORDS_READY_COMPONENT, PlayerInteractEntityEvent.class, 0);

        addActivatableComponent(SwordsComponents.SWORDS_ACTIVATE_ON_READY_COMPONENT, PlayerToggleSneakEvent.class, 0);
        addActivatableComponent(RageSpikeComponents.RAGE_SPIKE_ACTIVATE_COMPONENT, PlayerToggleSneakEvent.class, 1);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.RAGE_SPIKE_DISPLAY_ITEM_HEADER;
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(SwordsConfigFile.RAGE_SPIKE_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Swords.SWORDS_KEY;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "rage_spike";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        RageSpikeActivateEvent rageSpikeActivateEvent = new RageSpikeActivateEvent(abilityHolder);
        Bukkit.getPluginManager().callEvent(rageSpikeActivateEvent);

        if (!rageSpikeActivateEvent.isCancelled() && Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player) {
            abilityHolder.unreadyHolder();
            performRageSpike(abilityHolder, player);
            putHolderOnCooldown(abilityHolder);
        }
    }

    @Override
    public void comboActivate(@NotNull AbilityHolder abilityHolder) {
        RageSpikeActivateEvent rageSpikeActivateEvent = new RageSpikeActivateEvent(abilityHolder);
        Bukkit.getPluginManager().callEvent(rageSpikeActivateEvent);

        if (!rageSpikeActivateEvent.isCancelled() && Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player) {
            performRageSpike(abilityHolder, player);
        }
    }

    @Override
    public int getHungerCost(@NotNull AbilityHolder abilityHolder) {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.COMBO_CONFIG)
                .getInt(ComboConfigFile.RAGE_SPIKE_HUNGER_COST, 6);
    }

    /**
     * Executes the core Rage Spike effect — launching the player forward and damaging
     * entities they pass through. Called by both the ready-state path and the combo path.
     *
     * @param abilityHolder The {@link AbilityHolder} activating the ability.
     * @param player        The online {@link Player} associated with the holder.
     */
    private void performRageSpike(@NotNull AbilityHolder abilityHolder, @NotNull Player player) {
        int tier = getCurrentAbilityTier(abilityHolder);
        Vector unitVector = new Vector(player.getLocation().getDirection().getX(), 0, player.getLocation().getDirection().getZ());
        player.setVelocity(unitVector.multiply(getVelocity(tier)));

        // Launch sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.0f, 1.4f);

        RageSpike rageSpike = this;
        abilityHolder.addActiveAbility(rageSpike);
        AtomicInteger count = new AtomicInteger(0);
        List<UUID> entities = new ArrayList<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!abilityHolder.isAbilityActive(rageSpike) || !player.isOnline()
                        || player.isDead() || player.isSleeping()
                        || count.incrementAndGet() == 21) {
                    abilityHolder.removeActiveAbility(rageSpike);
                    cancel();
                } else {
                    // Dash trail: cloud puffs at feet + crit sparkle at chest
                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.1, 0), 4, 0.15, 0.05, 0.15, 0.02);
                    player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 0.8, 0), 6, 0.25, 0.25, 0.25, 0.05);

                    for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
                        if (entity instanceof LivingEntity livingEntity && !isNPC(entity) && !entities.contains(entity.getUniqueId())) {
                            RageSpikeDamageEvent rageSpikeDamageEvent = new RageSpikeDamageEvent(abilityHolder, livingEntity, getDamage(tier));
                            Bukkit.getPluginManager().callEvent(rageSpikeDamageEvent);
                            if (rageSpikeDamageEvent.isCancelled()) {
                                continue;
                            }
                            Vector targVector = new Vector(entity.getLocation().getDirection().getX(), entity.getLocation().getDirection().getY(), player.getLocation().getDirection().getZ());
                            entity.setVelocity(targVector.multiply(-4.3));
                            livingEntity.damage(rageSpikeDamageEvent.getDamage());
                            entities.add(entity.getUniqueId());
                            // Hit confirmation particles on the struck entity
                            player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, livingEntity.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.02);
                        }
                    }
                }
            }
        }.runTaskTimer(getPlugin(), 0, 1);
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return SwordsConfigFile.RAGE_SPIKE_ENABLED;
    }

    @NotNull
    @Override
    public SwordReadyData getReadyData() {
        return new SwordReadyData();
    }

    /**
     * Gets the damage to deal to enemies for the given tier.
     *
     * @param tier The tier to get the damage for
     * @return The amount of damage to deal to enemies for the given tier.
     */
    public double getDamage(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "damage");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "damage");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getDouble(tierRoute);
        } else {
            return swordsConfig.getDouble(allTiersRoute);
        }
    }

    /**
     * Gets the velocity to set the enemies to for the given tier.
     *
     * @param tier The tier to get the velocity for.
     * @return The velocity to set the enemies to for the given tier.
     */
    public int getVelocity(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "velocity");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "velocity");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getInt(tierRoute);
        } else {
            return swordsConfig.getInt(allTiersRoute, 5);
        }
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableActiveAbility.super.getApplicableAttributes();
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(AbilityItemPlaceholderKeys.DAMAGE.getKey(),
                Double.toString(getDamage(getCurrentAbilityTier(player.asSkillHolder()))));
        placeholders.put(AbilityItemPlaceholderKeys.COOLDOWN.getKey(),
                Long.toString(getCooldown(player.asSkillHolder())));
        return placeholders;
    }

    /**
     * Checks to see if the provided {@link Entity} is an NPC.
     *
     * @param entity The {@link Entity} to check.
     * @return {@code true} if the provided {@link Entity} is an NPC.
     */
    private boolean isNPC(@Nullable Entity entity) {
        return (entity == null || entity.hasMetadata("NPC") || entity instanceof NPC || entity.getClass().getName().equalsIgnoreCase("cofh.entity.PlayerFake") || entity instanceof ArmorStand);
    }
}
