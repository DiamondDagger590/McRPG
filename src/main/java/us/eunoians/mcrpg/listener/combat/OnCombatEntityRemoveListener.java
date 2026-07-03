package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;

import java.util.UUID;

/**
 * Handles entity removal from the world (despawn, chunk unload, plugin removal).
 * Removes the entity from all participant rosters. Players are handled by
 * {@link OnCombatPlayerQuitListener}.
 */
public class OnCombatEntityRemoveListener implements Listener {

    private final CombatTrackerManager combatTrackerManager;

    /**
     * Constructs a new {@link OnCombatEntityRemoveListener}.
     *
     * @param combatTrackerManager The {@link CombatTrackerManager} to report events to.
     */
    public OnCombatEntityRemoveListener(@NotNull CombatTrackerManager combatTrackerManager) {
        this.combatTrackerManager = combatTrackerManager;
    }

    /**
     * Handles entity removal from the world. Removes the entity from all sessions' participant
     * rosters. Skips players — player removal is handled by {@link OnCombatPlayerQuitListener}.
     *
     * @param event The entity remove event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRemove(@NotNull EntityRemoveEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        UUID removedEntityUUID = event.getEntity().getUniqueId();
        combatTrackerManager.removeParticipantFromAllSessions(removedEntityUUID, ParticipantRemovalReason.DESPAWN);
    }
}
