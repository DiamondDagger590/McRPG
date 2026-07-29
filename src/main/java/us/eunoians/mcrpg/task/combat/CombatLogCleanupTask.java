package us.eunoians.mcrpg.task.combat;

import com.diamonddagger590.mccore.configuration.common.ReloadableInteger;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.database.table.CombatLogDAO;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.logging.Level;

/**
 * Periodic task that deletes combat log audit trail entries older than the
 * configured retention period. {@link #runInitialCleanup()} is called once right after
 * construction (so servers that restart frequently still clean up), and
 * {@link #onIntervalComplete()} repeats the same cleanup on the configured interval for
 * long-running servers. A retention value of {@code 0} or negative disables cleanup.
 */
public class CombatLogCleanupTask extends CancelableCoreTask {

    private final McRPG mcRPG;
    private final ReloadableInteger retentionDays;

    /**
     * Constructs a new {@link CombatLogCleanupTask}.
     *
     * @param mcRPG              The plugin instance.
     * @param runIntervalSeconds Seconds between cleanup passes ({@link #onIntervalComplete()}).
     *                           The initial delay is always {@code 0} — {@link #onDelayComplete()}
     *                           is a no-op, so the first firing doesn't duplicate
     *                           {@link #runInitialCleanup()}.
     */
    public CombatLogCleanupTask(@NotNull McRPG mcRPG, double runIntervalSeconds) {
        super(mcRPG, 0, runIntervalSeconds);
        this.mcRPG = mcRPG;
        var config = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.COMBAT_CONFIG);
        this.retentionDays = new ReloadableInteger(config,
                CombatConfigFile.AUDIT_RETENTION_DAYS);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(ManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(Set.of(retentionDays));
    }

    /**
     * Performs the initial cleanup at startup. Called once by the bootstrap right
     * after construction, before {@link #runTask()} schedules the periodic cadence.
     */
    public void runInitialCleanup() {
        performCleanup();
    }

    /**
     * Repeats the cleanup on the configured interval — the periodic fallback for
     * long-running servers that don't restart often enough to rely on
     * {@link #runInitialCleanup()} alone.
     */
    @Override
    protected void onIntervalComplete() {
        performCleanup();
    }

    @Override
    protected void onDelayComplete() {
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected void onIntervalStart() {
    }

    @Override
    protected void onIntervalPause() {
    }

    @Override
    protected void onIntervalResume() {
    }

    /**
     * Submits the retention-based delete to the database executor.
     */
    private void performCleanup() {
        int days = retentionDays.getContent();
        if (days <= 0) {
            return;
        }

        Instant cutoff = mcRPG.getTimeProvider().now().minus(days, ChronoUnit.DAYS);
        var database = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection conn = database.getConnection()) {
                int deleted = CombatLogDAO.deleteOlderThan(conn, cutoff);
                if (deleted > 0) {
                    mcRPG.getLogger().info("Cleaned up " + deleted
                            + " combat log entries older than " + days + " days");
                }
            }
            catch (Exception e) {
                mcRPG.getLogger().log(Level.WARNING,
                        "Failed to clean up expired combat log entries", e);
            }
        });
    }
}
