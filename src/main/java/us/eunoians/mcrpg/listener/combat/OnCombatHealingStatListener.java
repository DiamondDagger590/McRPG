package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;

/**
 * Observes {@link EntityRegainHealthEvent} at {@link EventPriority#MONITOR} priority for passive
 * {@code healing_received} tracking. If the healed entity has an active combat session, increments
 * {@code healing_received}. Does NOT create sessions or add participants — healing is a
 * combat-adjacent interaction.
 * <p>
 * {@code healing_dealt} attribution is NOT tracked by this listener — Bukkit's
 * {@link EntityRegainHealthEvent} carries no healer source. See
 * {@link CombatTrackerManager#reportHealing(java.util.UUID, java.util.UUID, double)} for the
 * attribution approach.
 */
public class OnCombatHealingStatListener implements Listener {

    private final CombatTrackerManager combatTrackerManager;

    /**
     * Constructs a new {@link OnCombatHealingStatListener}.
     *
     * @param combatTrackerManager The {@link CombatTrackerManager} for session lookups.
     */
    public OnCombatHealingStatListener(@NotNull CombatTrackerManager combatTrackerManager) {
        this.combatTrackerManager = combatTrackerManager;
    }

    /**
     * Tracks {@code healing_received} on the healed entity's active session. Does not create
     * sessions or add participants — healing is a combat-adjacent interaction.
     *
     * @param event The health regain event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRegainHealth(@NotNull EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }
        combatTrackerManager.getSession(livingEntity.getUniqueId()).ifPresent(session ->
                session.getStatistics().incrementDouble(CombatSessionStatisticKey.HEALING_RECEIVED, event.getAmount()));
    }
}
