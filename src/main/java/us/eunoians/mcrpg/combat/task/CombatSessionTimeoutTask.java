package us.eunoians.mcrpg.combat.task;

import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatTrackerManager;

/**
 * Global repeating task that scans all active combat sessions for timeout on the main thread. The
 * scan logic itself lives in {@link CombatTrackerManager#scanSessionsForTimeout()}; this task is a
 * thin scheduler wrapper that invokes it at the configured cadence, so the session-lifecycle logic
 * (participant removal, session ending, condition hold-open) stays owned by the manager.
 */
public class CombatSessionTimeoutTask extends CancelableCoreTask {

    private final CombatTrackerManager combatTrackerManager;

    /**
     * Constructs a new {@link CombatSessionTimeoutTask}.
     *
     * @param plugin               The {@link McRPG} plugin instance.
     * @param combatTrackerManager The {@link CombatTrackerManager} whose sessions to scan.
     * @param scanIntervalSeconds  The interval in seconds between timeout scans.
     */
    public CombatSessionTimeoutTask(@NotNull McRPG plugin,
                                    @NotNull CombatTrackerManager combatTrackerManager,
                                    double scanIntervalSeconds) {
        super(plugin, 0, scanIntervalSeconds);
        this.combatTrackerManager = combatTrackerManager;
    }

    /**
     * Delegates the timeout scan to {@link CombatTrackerManager#scanSessionsForTimeout()}.
     */
    @Override
    protected void onIntervalComplete() {
        combatTrackerManager.scanSessionsForTimeout();
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
