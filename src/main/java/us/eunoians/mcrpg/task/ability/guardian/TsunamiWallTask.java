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
 * A task that drives a Tsunami Wall through two phases:
 * <ol>
 *   <li><b>Travel:</b> The wall moves from the spawn origin toward the destination,
 *       expanding from a narrow column to its full configured width/height as it travels.</li>
 *   <li><b>Hold:</b> Once the wall reaches its destination, it stays stationary for
 *       the remaining duration, applying knockback and slowness to entities on contact.</li>
 * </ol>
 */
public final class TsunamiWallTask extends ExpireableCoreTask {

    private static final double TASK_FREQUENCY = 2 / 20.0;
    private static final double WALL_THICKNESS = 0.75;
    private static final double MIN_SCALE = 0.2;

    private final Location origin;
    private final Location destination;
    private final Vector wallRight;
    private final int fullWidth;
    private final int fullHeight;
    private final double knockbackStrength;
    private final int slownessAmplifier;
    private final int slownessDurationTicks;
    private final Vector forward;
    private final UUID casterUUID;
    private final double travelSpeed;
    private final double totalTravelDistance;
    private final Set<UUID> hitEntities;

    private Location currentCenter;
    private double distanceTraveled;
    private boolean arrived;

    /**
     * Creates a new Tsunami Wall task.
     *
     * @param plugin              The McRPG plugin instance
     * @param origin              The starting location of the wall near the caster
     * @param destination         The final destination where the wall will hold position
     * @param wallRight           The unit vector pointing along the wall's width (right direction)
     * @param fullWidth           The full width of the wall in blocks at destination
     * @param fullHeight          The full height of the wall in blocks at destination
     * @param knockbackStrength   The strength of knockback applied to entities hitting the wall
     * @param slownessAmplifier   The amplifier for the slowness effect (0-indexed)
     * @param slownessDurationTicks The duration of the slowness effect in ticks
     * @param forward             The forward-facing direction of the wall (travel and knockback direction)
     * @param casterUUID          The UUID of the player who created the wall
     * @param durationTicks       The total duration of the wall in ticks (travel + hold)
     * @param travelSpeed         The speed at which the wall travels in blocks per tick
     */
    public TsunamiWallTask(@NotNull McRPG plugin, @NotNull Location origin, @NotNull Location destination,
                           @NotNull Vector wallRight, int fullWidth, int fullHeight,
                           double knockbackStrength, int slownessAmplifier, int slownessDurationTicks,
                           @NotNull Vector forward, @NotNull UUID casterUUID,
                           int durationTicks, double travelSpeed) {
        super(plugin, 0.0, TASK_FREQUENCY, (long) Math.ceil(durationTicks / 20.0));
        this.origin = origin.clone();
        this.destination = destination.clone();
        this.wallRight = wallRight.clone().normalize();
        this.fullWidth = fullWidth;
        this.fullHeight = fullHeight;
        this.knockbackStrength = knockbackStrength;
        this.slownessAmplifier = slownessAmplifier;
        this.slownessDurationTicks = slownessDurationTicks;
        this.forward = forward.clone().normalize();
        this.casterUUID = casterUUID;
        this.travelSpeed = travelSpeed;
        this.totalTravelDistance = origin.distance(destination);
        this.hitEntities = new HashSet<>();
        this.currentCenter = origin.clone();
        this.distanceTraveled = 0;
        this.arrived = false;
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
        World world = currentCenter.getWorld();
        if (world == null) {
            this.cancelTask();
            return;
        }

        if (!world.isChunkLoaded(currentCenter.getBlockX() >> 4, currentCenter.getBlockZ() >> 4)) {
            this.cancelTask();
            return;
        }

        if (!arrived) {
            advanceWall();
        }

        double scale = calculateScale();
        double effectiveWidth = fullWidth * scale;
        double effectiveHeight = fullHeight * scale;

        renderWallParticles(world, effectiveWidth, effectiveHeight);
        applyWallEffects(world, effectiveWidth, effectiveHeight);
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }

    /**
     * Advances the wall position along the forward direction by the travel speed.
     */
    private void advanceWall() {
        double step = travelSpeed * (TASK_FREQUENCY * 20);
        distanceTraveled += step;

        if (distanceTraveled >= totalTravelDistance) {
            distanceTraveled = totalTravelDistance;
            currentCenter = destination.clone();
            arrived = true;
        } else {
            currentCenter = origin.clone().add(forward.clone().multiply(distanceTraveled));
        }
    }

    /**
     * Calculates the current scale factor (0 to 1) based on travel progress.
     * The wall starts at {@link #MIN_SCALE} and grows linearly to full size upon arrival.
     *
     * @return The current scale factor between {@link #MIN_SCALE} and 1.0.
     */
    private double calculateScale() {
        if (arrived || totalTravelDistance <= 0) {
            return 1.0;
        }
        double progress = distanceTraveled / totalTravelDistance;
        return MIN_SCALE + (1.0 - MIN_SCALE) * progress;
    }

    /**
     * Checks for entities within the wall bounds and applies knockback and slowness.
     *
     * @param world           The world containing the wall.
     * @param effectiveWidth  The current width of the wall.
     * @param effectiveHeight The current height of the wall.
     */
    private void applyWallEffects(@NotNull World world, double effectiveWidth, double effectiveHeight) {
        double halfWidth = effectiveWidth / 2.0;
        double searchRadius = Math.max(halfWidth, effectiveHeight);

        for (Entity entity : world.getNearbyEntities(currentCenter, searchRadius, effectiveHeight, searchRadius)) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            if (entity.getUniqueId().equals(casterUUID)) {
                continue;
            }

            if (entity instanceof Player target && isAllied(casterUUID, target)) {
                continue;
            }

            if (!isWithinWallBounds(entity.getLocation(), effectiveWidth, effectiveHeight)) {
                continue;
            }

            Vector knockback = forward.clone().multiply(knockbackStrength);
            knockback.setY(0.2);

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

            livingEntity.setVelocity(livingEntity.getVelocity().add(contactEvent.getKnockbackVector()));

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

    /**
     * Renders water particles along the wall grid at the current size.
     *
     * @param world           The world to spawn particles in.
     * @param effectiveWidth  The current width of the wall.
     * @param effectiveHeight The current height of the wall.
     */
    private void renderWallParticles(@NotNull World world, double effectiveWidth, double effectiveHeight) {
        double halfWidth = effectiveWidth / 2.0;

        for (double w = -halfWidth; w <= halfWidth; w += 0.5) {
            for (double h = 0; h <= effectiveHeight; h += 0.5) {
                double x = currentCenter.getX() + wallRight.getX() * w;
                double y = currentCenter.getY() + h;
                double z = currentCenter.getZ() + wallRight.getZ() * w;

                world.spawnParticle(Particle.DRIPPING_WATER, x, y, z, 1, 0.05, 0.05, 0.05, 0);
            }
        }
    }

    /**
     * Checks if a location is within the wall's rectangular bounds at the given effective size.
     *
     * @param location        The location to check.
     * @param effectiveWidth  The current width of the wall.
     * @param effectiveHeight The current height of the wall.
     * @return {@code true} if the location is within the wall bounds.
     */
    private boolean isWithinWallBounds(@NotNull Location location, double effectiveWidth, double effectiveHeight) {
        Vector offset = location.toVector().subtract(currentCenter.toVector());

        double verticalOffset = offset.getY();
        if (verticalOffset < 0 || verticalOffset > effectiveHeight) {
            return false;
        }

        double widthOffset = offset.dot(wallRight);
        double halfWidth = effectiveWidth / 2.0;
        if (Math.abs(widthOffset) > halfWidth) {
            return false;
        }

        double depthOffset = offset.dot(forward);
        return Math.abs(depthOffset) <= WALL_THICKNESS;
    }

    /**
     * Checks if the target player is allied with the caster.
     * <p>
     * TODO: Implement with party system when available.
     *
     * @param casterUUID The UUID of the caster.
     * @param target     The target player to check.
     * @return {@code true} if the target is allied with the caster, {@code false} otherwise.
     */
    private boolean isAllied(@NotNull UUID casterUUID, @NotNull Player target) {
        // TODO: Implement with party system
        return false;
    }
}
