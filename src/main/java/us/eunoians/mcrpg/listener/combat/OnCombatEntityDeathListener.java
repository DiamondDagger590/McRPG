package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;

import java.util.UUID;

/**
 * Handles entity death — ends the dead entity's session and removes it from all
 * other sessions' participant rosters.
 */
public class OnCombatEntityDeathListener implements Listener {

    private final CombatTrackerManager combatTrackerManager;

    /**
     * Constructs a new {@link OnCombatEntityDeathListener}.
     *
     * @param combatTrackerManager The {@link CombatTrackerManager} to report events to.
     */
    public OnCombatEntityDeathListener(@NotNull CombatTrackerManager combatTrackerManager) {
        this.combatTrackerManager = combatTrackerManager;
    }

    /**
     * Handles entity death. Increments {@code kills} on the killer's session (if the killer is a
     * player with an active session), then ends the dead entity's session (if any) and removes it
     * from all other sessions' participant rosters.
     *
     * @param event The death event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        UUID deadEntityUUID = event.getEntity().getUniqueId();

        // Track kills — must happen BEFORE endSession removes the dead entity's session
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            combatTrackerManager.getSession(killer.getUniqueId())
                    .ifPresent(session -> session.getStatistics()
                            .incrementLong(CombatSessionStatisticKey.KILLS, 1));
        }

        combatTrackerManager.endSession(deadEntityUUID, CombatSessionEndReason.DEATH);
        combatTrackerManager.removeParticipantFromAllSessions(deadEntityUUID, ParticipantRemovalReason.DEATH);
    }
}
