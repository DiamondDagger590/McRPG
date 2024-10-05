package us.eunoians.mcrpg.ability.impl.swords;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableActiveAbility;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.ability.ready.SwordReadyData;
import us.eunoians.mcrpg.event.event.ability.swords.RageSpikeActivateEvent;
import us.eunoians.mcrpg.event.event.ability.swords.RageSpikeDamageEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rage Spike is an active ability that activates after the user readies their
 * sword and then crouches, blasting them forward and knocking back enemies and doing damage.
 */
public final class RageSpike extends McRPGAbility implements ConfigurableActiveAbility {

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
        return getPlugin().getFileManager().getFile(FileType.SWORDS_CONFIG);
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(SwordsConfigFile.RAGE_SPIKE_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("rage_spike");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Rage Spike";
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        int currentTier = getCurrentAbilityTier(mcRPGPlayer.asSkillHolder());
        return List.of("<gray>Ready your sword, then crouch to blast forward, damaging and knocking back foes.",
                "<gray>Damage: <gold>" + getDamage(currentTier));
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.IRON_SWORD);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        RageSpikeActivateEvent rageSpikeActivateEvent = new RageSpikeActivateEvent(abilityHolder);
        Bukkit.getPluginManager().callEvent(rageSpikeActivateEvent);

        if (!rageSpikeActivateEvent.isCancelled() && Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player) {
            abilityHolder.unreadyHolder();
            int tier = getCurrentAbilityTier(abilityHolder);
            Vector unitVector = new Vector(player.getLocation().getDirection().getX(), 0, player.getLocation().getDirection().getZ());
            player.setVelocity(unitVector.multiply(getVelocity(tier)));

            RageSpike rageSpike = this;
            abilityHolder.addActiveAbility(rageSpike);
            // After they've traveled we need to iteratee 20 times (1 second)
            AtomicInteger count = new AtomicInteger(0);
            //A list of all entities hit by rage spike so we arent double hitting
            List<UUID> entities = new ArrayList<>();
            //Damage entities as we fly by
            new BukkitRunnable() {
                @Override
                public void run() {
                    //verify that this runs 20 times
                    if (!abilityHolder.isAbilityActive(rageSpike) || !player.isOnline()
                            || player.isDead() || player.isSleeping()
                            || count.incrementAndGet() == 21) {
                        abilityHolder.removeActiveAbility(rageSpike);
                        cancel();
                    } else {
                        //get all the entities in a 2 by 2 radius
                        for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
                            //if the entity is living (avoids items and such) and isnt already hit
                            if (entity instanceof LivingEntity livingEntity && !isNPC(entity) && !entities.contains(entity.getUniqueId())) {
                                RageSpikeDamageEvent rageSpikeDamageEvent = new RageSpikeDamageEvent(abilityHolder, livingEntity, getDamage(tier));
                                Bukkit.getPluginManager().callEvent(rageSpikeDamageEvent);
                                if (rageSpikeDamageEvent.isCancelled()) {
                                    continue;
                                }
                                //make target go voom
                                Vector targVector = new Vector(entity.getLocation().getDirection().getX(), entity.getLocation().getDirection().getY(), player.getLocation().getDirection().getZ());
                                entity.setVelocity(targVector.multiply(-4.3));
                                //damage target and add them to list
                                livingEntity.damage(rageSpikeDamageEvent.getDamage());
                                entities.add(entity.getUniqueId());
                            }
                        }
                    }
                }
            }.runTaskTimer(getPlugin(), 0, 1);
            putHolderOnCooldown(abilityHolder);
        }
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(SwordsConfigFile.RAGE_SPIKE_ENABLED);
    }

    @NotNull
    @Override
    public Optional<ReadyData> getReadyData() {
        return Optional.of(new SwordReadyData());
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
