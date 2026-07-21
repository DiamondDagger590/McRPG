package us.eunoians.mcrpg.listener.combat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;

/**
 * Observes {@link EntityDamageByEntityEvent} at {@link EventPriority#MONITOR} priority (after
 * {@link OnCombatDamageListener} at {@link EventPriority#HIGHEST} has created/updated sessions).
 * Resolves the source entity (handling projectile shooters) and increments per-session damage and
 * hit statistics on the source's and target's active sessions.
 */
public class OnCombatDamageStatListener implements Listener {

    private final CombatTrackerManager combatTrackerManager;

    /**
     * Constructs a new {@link OnCombatDamageStatListener}.
     *
     * @param combatTrackerManager The {@link CombatTrackerManager} for session lookups.
     */
    public OnCombatDamageStatListener(@NotNull CombatTrackerManager combatTrackerManager) {
        this.combatTrackerManager = combatTrackerManager;
    }

    /**
     * Tracks per-session damage and hit statistics. Resolves the combatants via
     * {@link CombatDamageResolution#resolve(EntityDamageByEntityEvent)} — the same guards
     * {@link OnCombatDamageListener} applies when creating the sessions being written to here.
     * Increments {@code damage_dealt} and {@code hits_landed} on the source's session, and
     * {@code damage_taken} and {@code hits_received} on the target's session. Each side is written
     * independently, so a session-less source does not suppress the target's stats or vice versa.
     *
     * @param event The damage event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        CombatDamageResolution.resolve(event).ifPresent(combatants -> {
            double damage = event.getFinalDamage();

            combatTrackerManager.getSession(combatants.sourceUUID()).ifPresent(session -> {
                session.getStatistics().incrementDouble(CombatSessionStatisticKey.DAMAGE_DEALT, damage);
                session.getStatistics().incrementLong(CombatSessionStatisticKey.HITS_LANDED, 1);
            });
            combatTrackerManager.getSession(combatants.targetUUID()).ifPresent(session -> {
                session.getStatistics().incrementDouble(CombatSessionStatisticKey.DAMAGE_TAKEN, damage);
                session.getStatistics().incrementLong(CombatSessionStatisticKey.HITS_RECEIVED, 1);
            });
        });
    }
}
