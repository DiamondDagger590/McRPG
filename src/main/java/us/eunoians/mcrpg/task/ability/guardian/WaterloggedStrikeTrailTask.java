package us.eunoians.mcrpg.task.ability.guardian;

import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * A task that renders a water trail behind a Waterlogged Strike projectile.
 * Spawns dense water particles each tick while the projectile is alive and within range.
 * Automatically cancels when the projectile dies, hits something, or exceeds max range.
 */
public final class WaterloggedStrikeTrailTask extends ExpireableCoreTask {

    private static final double TASK_FREQUENCY = 1 / 20.0;
    private static final long SAFETY_TIMEOUT = 10;

    private final Snowball projectile;
    private final Location origin;
    private final double maxRangeSquared;

    /**
     * Creates a new Waterlogged Strike trail task.
     *
     * @param plugin     The McRPG plugin instance
     * @param projectile The snowball projectile to track
     * @param origin     The origin location where the projectile was launched
     * @param maxRange   The maximum range in blocks before the trail stops
     */
    public WaterloggedStrikeTrailTask(@NotNull McRPG plugin, @NotNull Snowball projectile,
                                      @NotNull Location origin, int maxRange) {
        super(plugin, 0, TASK_FREQUENCY, SAFETY_TIMEOUT);
        this.projectile = projectile;
        this.origin = origin.clone();
        this.maxRangeSquared = (double) maxRange * maxRange;
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
        // Cancel if projectile is no longer valid
        if (projectile.isDead() || !projectile.isValid() || projectile.isOnGround()) {
            this.cancelTask();
            return;
        }

        Location projectileLocation = projectile.getLocation();

        // Cancel if out of range
        if (projectileLocation.distanceSquared(origin) > maxRangeSquared) {
            this.cancelTask();
            return;
        }

        World world = projectileLocation.getWorld();
        if (world == null) {
            this.cancelTask();
            return;
        }

        // Spawn dense water particles at the projectile's current location
        world.spawnParticle(
                Particle.DRIPPING_WATER,
                projectileLocation,
                8,
                0.1, 0.1, 0.1,
                0.01
        );

        world.spawnParticle(
                Particle.SPLASH,
                projectileLocation,
                4,
                0.15, 0.15, 0.15,
                0.02
        );
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }
}
