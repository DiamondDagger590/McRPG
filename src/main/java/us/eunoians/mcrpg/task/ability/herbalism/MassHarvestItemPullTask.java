package us.eunoians.mcrpg.task.ability.herbalism;

import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.HashSet;
import java.util.Set;

/**
 * This task pulls items dropped by {@link MassHarvestPulseTask} towards the player
 * who initiated the task.
 */
public class MassHarvestItemPullTask extends ExpireableCoreTask {

    private static final double MINIMUM_DISTANCE_SQUARED = Math.pow(0.6, 2);
    private static final double MAX_RADIUS_PADDING = 1.5;
    private final Player player;
    private final Set<Item> itemsToPull;
    private final double maxRadiusSquared;

    public MassHarvestItemPullTask(@NotNull McRPG plugin, Player player, double taskDelay, double maxRadiusSquared) {
        super(plugin, taskDelay, 70L);
        this.player = player;
        this.maxRadiusSquared = Math.pow(Math.max(0, maxRadiusSquared) + MAX_RADIUS_PADDING, 2);
        this.itemsToPull = new HashSet<>();
    }

    public MassHarvestItemPullTask(@NotNull McRPG plugin, Player player, double taskDelay, double maxRadiusSquared, @NotNull Set<Item> itemsToPull) {
        super(plugin, taskDelay, 70L);
        this.player = player;
        this.maxRadiusSquared = Math.pow(Math.max(0, maxRadiusSquared) + MAX_RADIUS_PADDING, 2);
        this.itemsToPull = itemsToPull;
    }

    public void addItemToPull(@NotNull Item item){
        itemsToPull.add(item);
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
            cancelTask();
            return;
        }
        for (Item item : itemsToPull) {
            if (item.isDead() || !item.isValid()) {
                continue;
            }
            Location playerLocation = player.getLocation().add(0, 0.5, 0); // pull toward chest area
            Location itemLocation = item.getLocation();
            Vector toPlayer = playerLocation.toVector().subtract(itemLocation.toVector());
            double dist = playerLocation.distanceSquared(itemLocation);
            if (dist < MINIMUM_DISTANCE_SQUARED || dist > maxRadiusSquared) {
                item.setGravity(true);
                continue;
            }
            item.setGravity(false);
            Vector direction = toPlayer.multiply(1.0 / dist);
            // Tweak these to taste
            double speed = Math.min(0.55, 0.12 + Math.sqrt(dist) * 0.08); // caps max speed
            Vector vel = direction.multiply(speed);
            // Add a little lift so items don't get stuck on farmland edges
            vel.setY(Math.min(0.25, vel.getY() + 0.05));
            item.setVelocity(vel);
        }
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }
}
