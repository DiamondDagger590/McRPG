package us.eunoians.mcrpg.listener.combat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;

import java.util.UUID;

/**
 * Handles player logout — ends the player's session and removes them from all
 * other sessions' participant rosters.
 */
public class OnCombatPlayerQuitListener implements Listener {

    private final CombatTrackerManager combatTrackerManager;

    /**
     * Constructs a new {@link OnCombatPlayerQuitListener}.
     *
     * @param combatTrackerManager The {@link CombatTrackerManager} to report events to.
     */
    public OnCombatPlayerQuitListener(@NotNull CombatTrackerManager combatTrackerManager) {
        this.combatTrackerManager = combatTrackerManager;
    }

    /**
     * Handles player logout. Ends the player's session with reason {@link CombatSessionEndReason#LOGOUT}
     * and removes the player from all other sessions' participant rosters.
     * <p>
     * Note: {@link PlayerQuitEvent} is not cancellable, so {@code ignoreCancelled} is omitted.
     *
     * @param event The quit event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        combatTrackerManager.endSession(playerUUID, CombatSessionEndReason.LOGOUT);
        combatTrackerManager.removeParticipantFromAllSessions(playerUUID, ParticipantRemovalReason.LOGOUT);
    }
}
