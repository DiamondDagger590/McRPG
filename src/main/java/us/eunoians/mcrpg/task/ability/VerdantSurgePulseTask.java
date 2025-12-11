package us.eunoians.mcrpg.task.ability;

import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * The task that runs when {@link VerdantSurgePulseTask} is triggered. Each instance
 * of this task will emit a single wave of particles out to the maximum radius, growing all crops along the way.
 */
public class VerdantSurgePulseTask extends ExpireableCoreTask {

    private static final double TASK_FREQUENCY = .05;
    private final Player player;
    private Location center;
    private final double maxRadius;
    private double lastGrowthRadius;

    public VerdantSurgePulseTask(@NotNull McRPG plugin, @NotNull McRPGPlayer mcRPGPlayer, double delay, double maxRadius) {
        super(plugin, delay, TASK_FREQUENCY, 60L);
        this.player = mcRPGPlayer.getAsBukkitPlayer().orElseThrow(IllegalStateException::new);
        this.maxRadius = maxRadius;
        this.lastGrowthRadius = 0;
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
        if (!player.isOnline()) {
            this.cancelTask();
            return;
        }

        if (center == null) {
            center = player.getLocation().clone();
        }

        double progress = (this.getCurrentInterval() * TASK_FREQUENCY) / 2;
        // If our progress goes over the maximum radius, cancel the task
        if (progress > 1) {
            this.cancelTask();
            return;
        }
        double currentRadius = Math.min(maxRadius, progress * maxRadius);
        playRingParticles(center, currentRadius);

        if (this.getCurrentInterval() % 3 == 0) {
            growCropsInWaveShell(center, lastGrowthRadius, currentRadius);
            lastGrowthRadius = currentRadius;
        }
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }

    /**
     * Plays a circular particle effect around the given center location with the specified radius.
     * This method uses the {@link Particle#COMPOSTER} particle type to create a greenery effect.
     *
     * @param center The central location where the particle effect originates; must not be null
     * @param radius The current radius of the circle in which the particles are displayed; must be greater than 0
     */
    private void playRingParticles(@NotNull Location center, double radius) {
        World world = center.getWorld();
        if (world == null || radius <= 0) return;

        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16.0) {
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            double y = center.getY() + 0.1;

            world.spawnParticle(
                    Particle.COMPOSTER,
                    x, y, z,
                    3,
                    0.1, 0.1, 0.1,
                    0.01
            );
        }
    }

    /**
     * Grows crops in a spherical shell defined by an inner and outer radius around a central location.
     * All Ageable blocks within the radius shell have their growth stage incremented by one, up to their maximum age.
     * A visual particle effect is also displayed around the blocks as they grow.
     *
     * @param center The central location of the spherical shell; must not be null.
     * @param innerRadius The inner radius of the shell; blocks within this radius remain unaffected.
     * @param outerRadius The outer radius of the shell; blocks beyond this radius remain unaffected.
     */
    private void growCropsInWaveShell(@NotNull Location center, double innerRadius, double outerRadius) {
        World world = center.getWorld();
        if (world == null) return;

        // Add a small thickness to ensure blocks get hit even if tick steps are coarse
        double shellPadding = 0.75;
        double innerSq = Math.max(0, (innerRadius - shellPadding) * (innerRadius - shellPadding));
        double outerSq = (outerRadius + shellPadding) * (outerRadius + shellPadding);

        int blockRadius = (int) Math.ceil(outerRadius + shellPadding);

        Block centerBlock = center.getBlock();

        for (int dx = -blockRadius; dx <= blockRadius; dx++) {
            for (int dy = -1; dy <= 2; dy++) { // slightly above/below player feet
                for (int dz = -blockRadius; dz <= blockRadius; dz++) {

                    Block block = centerBlock.getRelative(dx, dy, dz);
                    Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);

                    double distSq = center.distanceSquared(blockCenter);
                    if (distSq < innerSq || distSq > outerSq) {
                        continue; // not in this tick's shell
                    }

                    BlockData data = block.getBlockData();
                    if (!(data instanceof Ageable ageable)) {
                        continue;
                    }

                    int age = ageable.getAge();
                    int max = ageable.getMaximumAge();
                    if (age >= max) {
                        continue;
                    }

                    ageable.setAge(age + 1);
                    block.setBlockData(ageable, true);

                    world.spawnParticle(
                            Particle.HAPPY_VILLAGER,
                            block.getLocation().add(0.5, 0.8, 0.5),
                            6,
                            0.25, 0.25, 0.25,
                            0.1
                    );
                }
            }
        }
    }
}
