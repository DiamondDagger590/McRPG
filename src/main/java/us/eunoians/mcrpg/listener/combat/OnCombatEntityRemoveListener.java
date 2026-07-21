package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;

/**
 * Handles entity removal from the world (despawn, chunk unload, plugin removal).
 * Removes the entity from all participant rosters. Players are handled by
 * {@link us.eunoians.mcrpg.listener.entity.player.PlayerLeaveListener}'s combat teardown.
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
     * rosters. Fast-skips the common cases that can never be combat participants: deaths (handled by
     * {@link OnCombatEntityDeathListener}), players (handled by
     * {@link us.eunoians.mcrpg.listener.entity.player.PlayerLeaveListener}'s combat teardown),
     * and non-living entities (items, projectiles, experience orbs) — which lets the frequent
     * chunk-unload and item-despawn removals return before touching the session map.
     *
     * @param event The entity remove event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRemove(@NotNull EntityRemoveEvent event) {
        if (event.getCause() == EntityRemoveEvent.Cause.DEATH) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity) || entity instanceof Player) {
            return;
        }
        combatTrackerManager.removeParticipantFromAllSessions(entity.getUniqueId(), ParticipantRemovalReason.DESPAWN);
    }
}
