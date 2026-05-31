package us.eunoians.mcrpg.task.ability.guardian;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.event.ability.guardian.WhirlpoolPullEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.UUID;

/**
 * A task that maintains a whirlpool zone, pulling nearby entities toward the center
 * and applying slowness. The whirlpool expands from a small initial radius to its full
 * size over a configurable expansion period, then holds at full size until expiration.
 */
public final class WhirlpoolZoneTask extends ExpireableCoreTask {

    private static final double MIN_SCALE = 0.15;

    private final Location center;
    private final double fullRadius;
    private final double pullVelocity;
    private final int slownessAmplifier;
    private final int slownessDurationTicks;
    private final UUID casterUUID;
    private final int expansionTicks;
    private double spiralAngle;
    private int elapsedTicks;

    /**
     * Creates a new Whirlpool Zone task.
     *
     * @param plugin              The McRPG plugin instance
     * @param center              The center location of the whirlpool
     * @param fullRadius          The full radius of the whirlpool's pull effect
     * @param pullVelocity        The velocity magnitude at which entities are pulled toward center
     * @param slownessAmplifier   The amplifier for the slowness effect (0-indexed)
     * @param slownessDurationTicks The duration of the slowness effect in ticks
     * @param casterUUID          The UUID of the player who created the whirlpool
     * @param durationTicks       The total duration of the whirlpool in ticks
     * @param tickInterval        The interval between pull ticks in ticks
     * @param expansionTicks      The number of ticks over which the whirlpool expands to full radius
     */
    public WhirlpoolZoneTask(@NotNull McRPG plugin, @NotNull Location center, double fullRadius, double pullVelocity,
                             int slownessAmplifier, int slownessDurationTicks, @NotNull UUID casterUUID,
                             int durationTicks, int tickInterval, int expansionTicks) {
        super(plugin, 0.0, tickInterval / 20.0, (long) Math.ceil(durationTicks / 20.0));
        this.center = center.clone();
        this.fullRadius = fullRadius;
        this.pullVelocity = pullVelocity;
        this.slownessAmplifier = slownessAmplifier;
        this.slownessDurationTicks = slownessDurationTicks;
        this.casterUUID = casterUUID;
        this.expansionTicks = expansionTicks;
        this.spiralAngle = 0;
        this.elapsedTicks = 0;
    }

    @Override
    protected void onTaskExpire() {

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void onDelayComplete() {

    }

    @Override
    protected void onIntervalStart() {

    }

    @Override
    protected void onIntervalComplete() {
        World world = center.getWorld();
        if (world == null) {
            this.cancelTask();
            return;
        }

        if (!world.isChunkLoaded(center.getBlockX() >> 4, center.getBlockZ() >> 4)) {
            this.cancelTask();
            return;
        }

        double currentRadius = calculateCurrentRadius();

        pullAndSlowEntities(world, currentRadius);
        spawnSpiralParticles(world, currentRadius);

        elapsedTicks++;
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }

    /**
     * Calculates the current effective radius based on expansion progress.
     * Starts at {@link #MIN_SCALE} of the full radius and grows linearly
     * to full size over the configured expansion period.
     *
     * @return The current effective radius.
     */
    private double calculateCurrentRadius() {
        if (expansionTicks <= 0 || elapsedTicks >= expansionTicks) {
            return fullRadius;
        }
        double progress = (double) elapsedTicks / expansionTicks;
        double scale = MIN_SCALE + (1.0 - MIN_SCALE) * progress;
        return fullRadius * scale;
    }

    /**
     * Pulls nearby entities toward the whirlpool center and applies slowness.
     *
     * @param world         The world to search for entities in.
     * @param currentRadius The current effective radius of the whirlpool.
     */
    private void pullAndSlowEntities(@NotNull World world, double currentRadius) {
        for (Entity entity : world.getNearbyEntities(center, currentRadius, currentRadius, currentRadius)) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            if (entity.getUniqueId().equals(casterUUID)) {
                continue;
            }

            if (entity instanceof Player target) {
                EntityManager entityManager = ((McRPG) getPlugin()).registryAccess()
                        .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY);
                if (entityManager.isAllied(casterUUID, target)) {
                    continue;
                }
            }

            if (entity.getLocation().distanceSquared(center) > currentRadius * currentRadius) {
                continue;
            }

            Vector pullDirection = center.toVector().subtract(entity.getLocation().toVector());
            if (pullDirection.lengthSquared() == 0) {
                continue;
            }
            pullDirection.normalize().multiply(pullVelocity);

            if (!(Bukkit.getEntity(casterUUID) instanceof LivingEntity caster)) {
                this.cancelTask();
                return;
            }
            WhirlpoolPullEvent pullEvent = new WhirlpoolPullEvent(caster, livingEntity, center, pullDirection.clone());
            Bukkit.getPluginManager().callEvent(pullEvent);
            if (pullEvent.isCancelled()) {
                continue;
            }

            livingEntity.setVelocity(livingEntity.getVelocity().add(pullEvent.getPullVector()));

            livingEntity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS,
                    slownessDurationTicks,
                    slownessAmplifier,
                    false,
                    true,
                    true
            ));
        }
    }

    /**
     * Spawns spiral water particles around the whirlpool center at the current radius.
     *
     * @param world         The world to spawn particles in.
     * @param currentRadius The current effective radius of the whirlpool.
     */
    private void spawnSpiralParticles(@NotNull World world, double currentRadius) {
        spiralAngle += Math.PI / 8;

        for (int arm = 0; arm < 3; arm++) {
            double armOffset = (2 * Math.PI / 3) * arm;
            for (double r = 0.5; r < currentRadius; r += 0.5) {
                double angle = spiralAngle + armOffset + (r * 0.5);
                double x = center.getX() + Math.cos(angle) * r;
                double z = center.getZ() + Math.sin(angle) * r;
                double y = center.getY() + 0.2;

                world.spawnParticle(Particle.DRIPPING_WATER, x, y, z, 1, 0.05, 0.05, 0.05, 0);
            }
        }
    }

}
