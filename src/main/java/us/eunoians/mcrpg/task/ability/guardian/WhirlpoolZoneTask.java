package us.eunoians.mcrpg.task.ability.guardian;

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
import us.eunoians.mcrpg.event.ability.guardian.WhirlpoolPullEvent;

import java.util.UUID;

/**
 * A task that maintains a whirlpool zone, pulling nearby entities toward the center
 * and applying slowness. Entities are pulled each tick interval and particles are
 * rendered in a spiral pattern.
 */
public final class WhirlpoolZoneTask extends ExpireableCoreTask {

    private final Location center;
    private final double radius;
    private final double pullVelocity;
    private final int slownessAmplifier;
    private final int slownessDurationTicks;
    private final UUID casterUUID;
    private double spiralAngle;

    /**
     * Creates a new Whirlpool Zone task.
     *
     * @param plugin              The McRPG plugin instance
     * @param center              The center location of the whirlpool
     * @param radius              The radius of the whirlpool's pull effect
     * @param pullVelocity        The velocity magnitude at which entities are pulled toward center
     * @param slownessAmplifier   The amplifier for the slowness effect (0-indexed)
     * @param slownessDurationTicks The duration of the slowness effect in ticks
     * @param casterUUID          The UUID of the player who created the whirlpool
     * @param durationTicks       The total duration of the whirlpool in ticks
     * @param tickInterval        The interval between pull ticks in ticks
     */
    public WhirlpoolZoneTask(@NotNull McRPG plugin, @NotNull Location center, double radius, double pullVelocity,
                             int slownessAmplifier, int slownessDurationTicks, @NotNull UUID casterUUID,
                             int durationTicks, int tickInterval) {
        super(plugin, 0.0, tickInterval / 20.0, (long) Math.ceil(durationTicks / 20.0));
        this.center = center.clone();
        this.radius = radius;
        this.pullVelocity = pullVelocity;
        this.slownessAmplifier = slownessAmplifier;
        this.slownessDurationTicks = slownessDurationTicks;
        this.casterUUID = casterUUID;
        this.spiralAngle = 0;
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

        // Check chunk is loaded
        if (!world.isChunkLoaded(center.getBlockX() >> 4, center.getBlockZ() >> 4)) {
            this.cancelTask();
            return;
        }

        // Pull entities and apply slowness
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            if (entity.getUniqueId().equals(casterUUID)) {
                continue;
            }

            if (entity instanceof Player target && isAllied(casterUUID, target)) {
                continue;
            }

            // Check actual distance (getNearbyEntities uses a bounding box)
            if (entity.getLocation().distanceSquared(center) > radius * radius) {
                continue;
            }

            // Calculate pull vector toward center
            Vector pullDirection = center.toVector().subtract(entity.getLocation().toVector());
            if (pullDirection.lengthSquared() == 0) {
                continue;
            }
            pullDirection.normalize().multiply(pullVelocity);

            // Fire event to allow cancellation
            Player caster = Bukkit.getPlayer(casterUUID);
            if (caster == null) {
                this.cancelTask();
                return;
            }
            WhirlpoolPullEvent pullEvent = new WhirlpoolPullEvent(caster, livingEntity, center, pullDirection.clone());
            Bukkit.getPluginManager().callEvent(pullEvent);
            if (pullEvent.isCancelled()) {
                continue;
            }

            livingEntity.setVelocity(livingEntity.getVelocity().add(pullEvent.getPullVector()));

            // Apply slowness
            livingEntity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS,
                    slownessDurationTicks,
                    slownessAmplifier,
                    false,
                    true,
                    true
            ));
        }

        // Spawn spiral particles
        spawnSpiralParticles(world);
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }

    /**
     * Spawns spiral water particles around the whirlpool center.
     *
     * @param world The world to spawn particles in
     */
    private void spawnSpiralParticles(@NotNull World world) {
        spiralAngle += Math.PI / 8;

        for (int arm = 0; arm < 3; arm++) {
            double armOffset = (2 * Math.PI / 3) * arm;
            for (double r = 0.5; r < radius; r += 0.5) {
                double angle = spiralAngle + armOffset + (r * 0.5);
                double x = center.getX() + Math.cos(angle) * r;
                double z = center.getZ() + Math.sin(angle) * r;
                double y = center.getY() + 0.2;

                world.spawnParticle(Particle.DRIPPING_WATER, x, y, z, 1, 0.05, 0.05, 0.05, 0);
            }
        }
    }

    /**
     * Checks if the target player is allied with the caster.
     * <p>
     * TODO: Implement with party system when available.
     *
     * @param casterUUID The UUID of the caster
     * @param target     The target player to check
     * @return {@code true} if the target is allied with the caster, {@code false} otherwise
     */
    private boolean isAllied(@NotNull UUID casterUUID, @NotNull Player target) {
        // TODO: Implement with party system
        return false;
    }
}
