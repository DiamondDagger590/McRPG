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
import us.eunoians.mcrpg.event.ability.guardian.TsunamiWallContactEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A task that maintains a Tsunami Wall, rendering water particles along a vertical plane
 * and applying knockback and slowness to entities that contact the wall.
 * The wall is defined by a center point, a right-direction vector, width, and height.
 */
public final class TsunamiWallTask extends ExpireableCoreTask {

    private static final double TASK_FREQUENCY = 2 / 20.0;
    private static final double WALL_THICKNESS = 0.75;

    private final Location wallCenter;
    private final Vector wallRight;
    private final int width;
    private final int height;
    private final double knockbackStrength;
    private final int slownessAmplifier;
    private final int slownessDurationTicks;
    private final Vector forward;
    private final UUID casterUUID;
    private final Set<UUID> hitEntities;

    /**
     * Creates a new Tsunami Wall task.
     *
     * @param plugin              The McRPG plugin instance
     * @param wallCenter          The center location of the wall's base
     * @param wallRight           The unit vector pointing along the wall's width (right direction)
     * @param width               The total width of the wall in blocks
     * @param height              The total height of the wall in blocks
     * @param knockbackStrength   The strength of knockback applied to entities hitting the wall
     * @param slownessAmplifier   The amplifier for the slowness effect (0-indexed)
     * @param slownessDurationTicks The duration of the slowness effect in ticks
     * @param forward             The forward-facing direction of the wall (knockback direction)
     * @param casterUUID          The UUID of the player who created the wall
     * @param durationTicks       The total duration of the wall in ticks
     */
    public TsunamiWallTask(@NotNull McRPG plugin, @NotNull Location wallCenter, @NotNull Vector wallRight,
                           int width, int height, double knockbackStrength, int slownessAmplifier,
                           int slownessDurationTicks, @NotNull Vector forward, @NotNull UUID casterUUID,
                           int durationTicks) {
        super(plugin, 0.0, TASK_FREQUENCY, (long) Math.ceil(durationTicks / 20.0));
        this.wallCenter = wallCenter.clone();
        this.wallRight = wallRight.clone().normalize();
        this.width = width;
        this.height = height;
        this.knockbackStrength = knockbackStrength;
        this.slownessAmplifier = slownessAmplifier;
        this.slownessDurationTicks = slownessDurationTicks;
        this.forward = forward.clone().normalize();
        this.casterUUID = casterUUID;
        this.hitEntities = new HashSet<>();
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
        World world = wallCenter.getWorld();
        if (world == null) {
            this.cancelTask();
            return;
        }

        // Check chunk is loaded
        if (!world.isChunkLoaded(wallCenter.getBlockX() >> 4, wallCenter.getBlockZ() >> 4)) {
            this.cancelTask();
            return;
        }

        // Render wall particles
        renderWallParticles(world);

        // Check for entities within the wall bounds
        double halfWidth = width / 2.0;
        double searchRadius = Math.max(halfWidth, height);

        for (Entity entity : world.getNearbyEntities(wallCenter, searchRadius, height, searchRadius)) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            if (entity.getUniqueId().equals(casterUUID)) {
                continue;
            }

            if (entity instanceof Player target && isAllied(casterUUID, target)) {
                continue;
            }

            // Check if entity is within the wall's bounds
            if (!isWithinWallBounds(entity.getLocation())) {
                continue;
            }

            // Compute knockback vector
            Vector knockback = forward.clone().multiply(knockbackStrength);
            knockback.setY(0.2);

            // Fire contact event
            Player caster = Bukkit.getPlayer(casterUUID);
            if (caster == null) {
                this.cancelTask();
                return;
            }
            TsunamiWallContactEvent contactEvent = new TsunamiWallContactEvent(
                    caster, livingEntity, knockback.clone(), slownessAmplifier, slownessDurationTicks);
            Bukkit.getPluginManager().callEvent(contactEvent);
            if (contactEvent.isCancelled()) {
                continue;
            }

            // Apply knockback from event (may have been modified)
            livingEntity.setVelocity(livingEntity.getVelocity().add(contactEvent.getKnockbackVector()));

            // Apply slowness from event (may have been modified)
            livingEntity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS,
                    contactEvent.getSlownessDurationTicks(),
                    contactEvent.getSlownessAmplifier(),
                    false,
                    true,
                    true
            ));

            hitEntities.add(entity.getUniqueId());
        }
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }

    /**
     * Renders water particles along the wall grid.
     *
     * @param world The world to spawn particles in
     */
    private void renderWallParticles(@NotNull World world) {
        double halfWidth = width / 2.0;

        for (double w = -halfWidth; w <= halfWidth; w += 0.5) {
            for (double h = 0; h <= height; h += 0.5) {
                double x = wallCenter.getX() + wallRight.getX() * w;
                double y = wallCenter.getY() + h;
                double z = wallCenter.getZ() + wallRight.getZ() * w;

                world.spawnParticle(Particle.DRIPPING_WATER, x, y, z, 1, 0.05, 0.05, 0.05, 0);
            }
        }
    }

    /**
     * Checks if a location is within the wall's rectangular bounds.
     *
     * @param location The location to check
     * @return {@code true} if the location is within the wall bounds
     */
    private boolean isWithinWallBounds(@NotNull Location location) {
        Vector offset = location.toVector().subtract(wallCenter.toVector());

        // Check height (vertical)
        double verticalOffset = offset.getY();
        if (verticalOffset < 0 || verticalOffset > height) {
            return false;
        }

        // Check width (along wallRight axis)
        double widthOffset = offset.dot(wallRight);
        double halfWidth = width / 2.0;
        if (Math.abs(widthOffset) > halfWidth) {
            return false;
        }

        // Check thickness (along forward axis)
        double depthOffset = offset.dot(forward);
        return Math.abs(depthOffset) <= WALL_THICKNESS;
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
