package us.eunoians.mcrpg.combat.task;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;
import us.eunoians.mcrpg.combat.condition.CombatCondition;
import us.eunoians.mcrpg.combat.condition.CombatConditionRegistry;
import us.eunoians.mcrpg.event.combat.CombatParticipantRemoveEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Global repeating task that scans all active combat sessions for timeout. For each session:
 * <ol>
 *   <li>Checks per-participant timeouts and removes stale participants.</li>
 *   <li>Checks whether the roster is now empty (all participants gone).</li>
 *   <li>Checks whether any registered {@link CombatCondition} holds the session open.</li>
 *   <li>Checks session-level timeout and ends expired sessions.</li>
 * </ol>
 * <p>
 * The task snapshots the active session keys before iterating to avoid
 * {@link java.util.ConcurrentModificationException} when sessions are removed during the scan.
 */
public class CombatSessionTimeoutTask extends CancelableCoreTask {

    private final CombatTrackerManager combatTrackerManager;

    /**
     * Constructs a new {@link CombatSessionTimeoutTask}.
     *
     * @param combatTrackerManager The {@link CombatTrackerManager} to scan for timed-out sessions.
     * @param scanIntervalSeconds  The interval in seconds between timeout scans.
     */
    public CombatSessionTimeoutTask(@NotNull CombatTrackerManager combatTrackerManager,
                                    double scanIntervalSeconds) {
        super(McRPG.getInstance(), 0, scanIntervalSeconds);
        this.combatTrackerManager = combatTrackerManager;
    }

    /**
     * Scans all active sessions for per-participant and session-level timeouts.
     * <p>
     * For each session:
     * <ol>
     *   <li>Find participants whose per-participant timer has expired and remove them
     *       (fires {@link CombatParticipantRemoveEvent} with reason
     *       {@link ParticipantRemovalReason#TIMEOUT}).</li>
     *   <li>If the roster is now empty, end the session with reason
     *       {@link CombatSessionEndReason#ALL_PARTICIPANTS_GONE}.</li>
     *   <li>If the session-level timer has expired, check all registered {@link CombatCondition}s
     *       via the {@link CombatConditionRegistry}. If any condition returns {@code true} for the
     *       session owner, reset the session timeout via {@link CombatSession#recordActivity()} and
     *       skip ending. Otherwise, end the session with reason
     *       {@link CombatSessionEndReason#TIMEOUT}.</li>
     * </ol>
     */
    @Override
    protected void onIntervalComplete() {
        CombatConditionRegistry conditionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.COMBAT_CONDITION);

        List<UUID> sessionKeys = new ArrayList<>(combatTrackerManager.getActiveSessions().keySet());

        for (UUID uuid : sessionKeys) {
            Optional<CombatSession> sessionOptional = combatTrackerManager.getSession(uuid);
            if (sessionOptional.isEmpty()) {
                continue;
            }

            CombatSession session = sessionOptional.get();

            List<CombatParticipant> timedOutParticipants = session.getTimedOutParticipants();
            for (CombatParticipant participant : timedOutParticipants) {
                CombatType previousType = session.getCombatType();
                session.removeParticipant(participant.getUUID());
                CombatType newType = session.getCombatType();
                Bukkit.getPluginManager().callEvent(
                        new CombatParticipantRemoveEvent(session, participant, ParticipantRemovalReason.TIMEOUT,
                                previousType, newType)
                );
            }

            if (session.isEmpty()) {
                combatTrackerManager.endSession(uuid, CombatSessionEndReason.ALL_PARTICIPANTS_GONE);
                continue;
            }

            if (session.isTimedOut()) {
                Player player = Bukkit.getPlayer(session.getEntityUUID());
                if (player != null) {
                    boolean heldOpen = false;
                    for (CombatCondition condition : conditionRegistry.getAll()) {
                        if (condition.isInCombat(player)) {
                            session.recordActivity();
                            heldOpen = true;
                            break;
                        }
                    }
                    if (heldOpen) {
                        continue;
                    }
                }
                combatTrackerManager.endSession(uuid, CombatSessionEndReason.TIMEOUT);
            }
        }
    }

    /**
     * Called when this task is cancelled. No cleanup is required.
     */
    @Override
    protected void onCancel() { }

    /**
     * Called when the initial delay completes before the first interval begins. No action is required.
     */
    @Override
    protected void onDelayComplete() { }

    /**
     * Called at the start of each interval before processing begins. No action is required.
     */
    @Override
    protected void onIntervalStart() { }

    /**
     * Called when this task is paused. No action is required.
     */
    @Override
    protected void onIntervalPause() { }

    /**
     * Called when this task is resumed after being paused. No action is required.
     */
    @Override
    protected void onIntervalResume() { }
}
