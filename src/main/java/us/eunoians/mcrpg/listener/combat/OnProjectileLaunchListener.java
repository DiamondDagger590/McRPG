package us.eunoians.mcrpg.listener.combat;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * Tags projectiles with a PDC launch timestamp at fire time. Stateless — does not depend
 * on the combat tracker manager. Downstream consumers (combo abilities, timing-based scaling)
 * can read the stored timestamp to compute flight time.
 */
public class OnProjectileLaunchListener implements Listener {

    /**
     * PDC key for the epoch millisecond timestamp when a projectile was launched.
     */
    public static final NamespacedKey PROJECTILE_LAUNCH_TIME_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "projectile_launch_time");

    /**
     * Tags projectiles with a PDC launch timestamp at fire time. Downstream consumers
     * (combo abilities, timing-based scaling) can read this to compute flight time.
     *
     * @param event The projectile launch event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(@NotNull ProjectileLaunchEvent event) {
        event.getEntity().getPersistentDataContainer().set(
                PROJECTILE_LAUNCH_TIME_KEY,
                PersistentDataType.LONG,
                McRPG.getInstance().getTimeProvider().now().toEpochMilli()
        );
    }
}
