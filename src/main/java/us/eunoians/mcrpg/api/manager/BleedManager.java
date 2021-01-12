package us.eunoians.mcrpg.api.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed;
import us.eunoians.mcrpg.api.event.swords.BleedDamageEvent;
import us.eunoians.mcrpg.api.event.swords.BleedEndEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the central manager for handling all things relating to the {@link Bleed}
 * ability and it's various effects. It abstractly calls the {@link BleedDamageEvent} for other abilities such as {@link us.eunoians.mcrpg.ability.impl.swords.Vampire}
 * to hook into.
 *
 * @author DiamondDagger590
 */
public class BleedManager implements Listener {

    /**
     * This {@link Map} contains all current {@link BleedTask}s that are issuing damage.
     * <p>
     * The {@link UUID} that is listed as a key represents the entity being affected by Bleed
     * rather than the person who issued the Bleed
     */
    private Map<UUID, BleedTask> bleedTasks = new HashMap<>();

    /**
     * This {@link Map} contains all entities who are immune to being afflicted with
     * {@link Bleed} and the time in millis that their
     * immunity wears off.
     * <p>
     * A entity's presence in this map doesn't indicate that they are currently immune from
     * {@link Bleed}
     */
    private Map<UUID, Long> bleedImmunityDuration = new HashMap<>();

    /**
     * This {@link Map} contains the amount of entities that are currently afflicted by
     * {@link Bleed} by the entity that has their
     * {@link UUID} stored as a key.
     * <p>
     * The reason this map exists is to prevent entities from triggering Bleed on multiple enemies
     * and becoming nearly invincible due to perks such as {@link us.eunoians.mcrpg.ability.impl.swords.Vampire}
     */
    private Map<UUID, Integer> amountOfEntitiesAffected = new HashMap<>();

    public BleedManager() {

    }

    /**
     * Handle removing bleed when an {@link LivingEntity} dies
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleDeath(EntityDeathEvent entityDeathEvent) {

        if (isCurrentlyBleeding(entityDeathEvent.getEntity().getUniqueId())) {
            cancelBleedTask(entityDeathEvent.getEntity().getUniqueId(), true);
        }
    }

    /**
     * Starts the Bleed process using the provided data
     *
     * @param inflicter          The {@link LivingEntity} inflicting the Bleed
     * @param affected           The {@link LivingEntity} being affected by the Bleed
     * @param cycleTickFrequency The amount of time in ticks that each cycle should run
     * @param damagePerCycle     The amount of damage to be dealt to the affected entity each cycle
     * @param amountOfCycles     The amount of cycles to be ran
     * @param restoreHealth      The amount of health to restore provided that health restoration is enabled
     * @param healthToRestore    If health should be restored on each Bleed cycle to the inflicter
     */
    public void startBleed(@NotNull LivingEntity inflicter, @NotNull LivingEntity affected, int cycleTickFrequency, int damagePerCycle, int amountOfCycles, boolean restoreHealth, int healthToRestore) {

        AtomicInteger cycles = new AtomicInteger(0);

        boolean isPlayer = affected instanceof Player;

        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {

                //Validate entities and player specific edge cases along with handle cycles
                if (cycles.get() >= amountOfCycles) {
                    cancelBleedTask(affected.getUniqueId(), true);
                    return;
                }
                else if (isPlayer && !((Player) affected).isOnline()) {
                    return;
                }
                else if (!affected.isValid()) {
                    cancelBleedTask(affected.getUniqueId(), true);
                    return;
                }
                else if (affected.isDead()) {
                    cancelBleedTask(affected.getUniqueId(), true);
                    return;
                }

                BleedDamageEvent bleedDamageEvent = new BleedDamageEvent(inflicter, affected, damagePerCycle, restoreHealth, healthToRestore);
                Bukkit.getPluginManager().callEvent(bleedDamageEvent);

                if (!bleedDamageEvent.isCancelled()) {

                    affected.damage(bleedDamageEvent.getDamage());

                    cycles.incrementAndGet();

                    //Due to our above checks we can assume the entity or player is online and valid
                    if (bleedDamageEvent.isRestoreHealth() && bleedDamageEvent.getHealthToRestore() > 0) {
                        inflicter.setHealth(Math.min(20, inflicter.getHealth() + bleedDamageEvent.getHealthToRestore()));
                    }
                }
            }
        }.runTaskTimer(McRPG.getInstance(), 0, cycleTickFrequency);

        BleedTask bleedTask = new BleedTask(bukkitTask, inflicter.getUniqueId(), affected.getUniqueId());
        bleedTasks.put(affected.getUniqueId(), bleedTask);

        amountOfEntitiesAffected.put(inflicter.getUniqueId(), getAmountOfEntitiesAffected(inflicter.getUniqueId()) + 1);
    }

    /**
     * Returns a {@link Collection} of {@link BleedTask} that are issuing Bleed damage.
     *
     * @return A {@link Collection} of {@link BleedTask} that are issuing Bleed damage
     */
    @NotNull
    public Collection<BleedTask> getBleedTasks() {
        return this.bleedTasks.values();
    }

    /**
     * Gets a specific {@link BleedTask} that is damaging the {@link org.bukkit.entity.LivingEntity} whose
     * {@link UUID} is provided.
     *
     * @param uuid The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} whose Bleed task is wanted
     * @return The {@link BleedTask} that is damaging the {@link org.bukkit.entity.LivingEntity} requested.
     * This can also return {@code null} if there is no such task present
     */
    @Nullable
    public BleedTask getBleedTask(@NotNull UUID uuid) {
        return this.bleedTasks.getOrDefault(uuid, null);
    }

    /**
     * Cancels the {@link BleedTask} for the {@link UUID} provided and removes it from the
     * {@link Collection}
     *
     * @param uuid The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} whose Bleed task is being cancelled
     * @return {@code true} if the {@link BleedTask} was successfully cancelled (was present)
     */
    public boolean cancelBleedTask(@NotNull UUID uuid) {
        BleedTask bleedTask = this.bleedTasks.remove(uuid);

        if (bleedTask != null) {

            bleedTask.getBukkitTask().cancel();

            UUID inflicterUUID = bleedTask.getInflicter();
            if (amountOfEntitiesAffected.containsKey(inflicterUUID)) {
                int newAmount = amountOfEntitiesAffected.get(inflicterUUID) - 1;

                if (newAmount <= 0) {
                    amountOfEntitiesAffected.remove(inflicterUUID);
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Cancels the {@link BleedTask} for the {@link UUID} provided and removes it from the
     * {@link Collection}
     *
     * @param uuid      The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} whose Bleed task is being cancelled
     * @param setImmune If the {@link UUID} should be set to immune after cancellation
     * @return {@code true} if the {@link BleedTask} was successfully cancelled (was present)
     */
    public boolean cancelBleedTask(@NotNull UUID uuid, boolean setImmune) {
        BleedTask bleedTask = this.bleedTasks.remove(uuid);

        if (bleedTask != null) {

            bleedTask.getBukkitTask().cancel();

            BleedEndEvent bleedEndEvent = new BleedEndEvent(bleedTask.getVictim(), bleedTask.getInflicter());
            Bukkit.getPluginManager().callEvent(bleedEndEvent);

            UUID inflicterUUID = bleedTask.getInflicter();
            if (amountOfEntitiesAffected.containsKey(inflicterUUID)) {
                int newAmount = amountOfEntitiesAffected.get(inflicterUUID) - 1;

                if (newAmount <= 0) {
                    amountOfEntitiesAffected.remove(inflicterUUID);
                }
            }

            if (setImmune) {
                startImmunity(uuid);
            }

            return true;
        }

        return false;
    }

    /**
     * Starts Bleed immunity for the {@link UUID} for a the duration
     * specified in the config
     *
     * @param uuid The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} to
     *             be put on Bleed immunity
     */
    public void startImmunity(@NotNull UUID uuid) {

        //TODO pull seconds from a config
        int seconds = 5;
        setImmunity(uuid, seconds);
    }

    /**
     * Sets the {@link org.bukkit.entity.LivingEntity} that is mapped to the provided
     * {@link UUID} on Bleed immunity for the specified amount of seconds
     *
     * @param uuid    The {@link UUID} that is being set to be immune
     * @param seconds The amount of seconds to put on Bleed Immunity for
     */
    public void setImmunity(@NotNull UUID uuid, long seconds) {
        bleedImmunityDuration.put(uuid, System.currentTimeMillis() + (seconds * 1000L));
    }

    /**
     * Gets the time that Bleed immunity expires for the {@link UUID} provided.
     * <p>
     * This returns {@code -1} if there is no active immunity.
     * <p>
     * This method also removes the {@link UUID} from the internal {@link Map} if
     * the immunity is no longer valid
     *
     * @param uuid The {@link UUID} to get the immunity expire time for
     * @return The expire time of the immunity in millis or {@code -1} if there is no active immunity
     */
    public long getImmunityExpireTime(@NotNull UUID uuid) {

        if (bleedImmunityDuration.containsKey(uuid)) {

            long expireTime = bleedImmunityDuration.get(uuid);

            if (expireTime > System.currentTimeMillis()) {
                return expireTime;
            }

            bleedImmunityDuration.remove(uuid);
        }

        return -1;
    }

    /**
     * Checks to see if the provided {@link UUID} os currently immune from being afflicted with Bleed
     *
     * @param uuid The {@link UUID} to be checked
     * @return {@code true} if the {@link UUID} is currently immune
     */
    public boolean isCurrentlyImmune(@NotNull UUID uuid) {
        return getImmunityExpireTime(uuid) != -1;
    }

    /**
     * Checks to see if a {@link org.bukkit.entity.LivingEntity} can inflict Bleed onto another
     * {@link org.bukkit.entity.LivingEntity} based on the normal Bleed checks.
     *
     * @param inflicter The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} that is trying to cause Bleed
     * @param affected  The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} that is going to Bleed
     * @return {@code true} if the inflicter can cause bleed on the target
     */
    public boolean canInflictBleed(@NotNull UUID inflicter, @NotNull UUID affected) {

        //TODO move this to config
        int maxBleedVictims = 2;

        boolean canInflict = getAmountOfEntitiesAffected(inflicter) < maxBleedVictims && !isCurrentlyImmune(affected) && !isCurrentlyBleeding(affected);

        return canInflict;
    }

    /**
     * Gets the amount of {@link org.bukkit.entity.Entity}s currently affected by the
     * {@link org.bukkit.entity.LivingEntity} mapped to the {@link UUID} provided.
     * <p>
     * This method also removes the {@link UUID} from the internal {@link Map}s if the amount
     * is 0.
     *
     * @param inflicter The {@link UUID} to check
     * @return The positive zero inclusive number of the amount of entities being affected by the inflicter
     */
    public int getAmountOfEntitiesAffected(@NotNull UUID inflicter) {

        int amount = 0;

        if (amountOfEntitiesAffected.containsKey(inflicter)) {

            amount = amountOfEntitiesAffected.get(inflicter);

            if (amount <= 0) {
                amountOfEntitiesAffected.remove(inflicter);
            }
        }

        return amount;
    }

    /**
     * Checks to see if the {@link org.bukkit.entity.LivingEntity} mapped to the provided
     * {@link UUID} is currently Bleeding
     *
     * @param uuid The {@link UUID} to check
     * @return {@code true} if the {@link UUID} is currently bleeding
     */
    public boolean isCurrentlyBleeding(@NotNull UUID uuid) {
        return getBleedTask(uuid) != null;
    }

    /**
     * This class serves as a wrapper object for {@link BukkitTask} that allows easy access
     * on data about who is being afflicted with Bleed and who issued it.
     *
     * @author DiamondDagger590
     */
    public class BleedTask {

        /**
         * The {@link BukkitTask} actually dealing the damage
         */
        private BukkitTask bukkitTask;

        /**
         * The {@link UUID} of a {@link org.bukkit.entity.LivingEntity}
         * that inflicted the target with Bleed
         */
        private UUID inflicter;

        /**
         * The {@link UUID} of a {@link org.bukkit.entity.LivingEntity}
         * affected by Bleed due to the inflictor.
         */
        private UUID victim;

        /**
         * @param bukkitTask The {@link BukkitTask} issuing the damage
         * @param inflicter  The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} that is causing the Bleed
         * @param victim     The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} that is being affected by the Bleed
         */
        public BleedTask(@NotNull BukkitTask bukkitTask, @NotNull UUID inflicter, @NotNull UUID victim) {
            this.bukkitTask = bukkitTask;
            this.inflicter = inflicter;
            this.victim = victim;
        }

        /**
         * Gets the {@link BukkitTask} that is inflicting damage
         *
         * @return The {@link BukkitTask} that is inflicting damage
         */
        @NotNull
        public BukkitTask getBukkitTask() {
            return bukkitTask;
        }

        /**
         * Gets the {@link UUID} of the {@link org.bukkit.entity.LivingEntity} that
         * caused the Bleed to occur
         *
         * @return The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} that
         * caused the Bleed to occur
         */
        @NotNull
        public UUID getInflicter() {
            return inflicter;
        }

        /**
         * Gets the {@link UUID} of the {@link org.bukkit.entity.LivingEntity} that
         * is affected by the Bleed
         *
         * @return The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} that
         * is affected by the Bleed
         */
        @NotNull
        public UUID getVictim() {
            return victim;
        }
    }
}
