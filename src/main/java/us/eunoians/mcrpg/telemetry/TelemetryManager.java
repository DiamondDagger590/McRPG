package us.eunoians.mcrpg.telemetry;

import com.diamonddagger590.mccore.manager.CoreManager;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.telemetry.MetricsSnapshot.AbilityMetrics;
import us.eunoians.mcrpg.telemetry.MetricsSnapshot.AbilityPair;
import us.eunoians.mcrpg.telemetry.MetricsSnapshot.LoadoutSnapshot;
import us.eunoians.mcrpg.telemetry.MetricsSnapshot.SkillMetrics;

import java.util.Map;

/**
 * Manages the telemetry lifecycle: initializing the accumulator, registering event listeners,
 * and scheduling periodic metric snapshots.
 * <p>
 * Telemetry is opt-in. When disabled in config, no listeners are registered, no tasks are
 * scheduled, and zero overhead is incurred.
 * <p>
 * Currently, snapshots are logged to the plugin logger. In the future, this is where
 * the HTTPS push to a remote telemetry backend would be implemented.
 */
public class TelemetryManager extends CoreManager<McRPG> {

    private MetricsAccumulator accumulator;
    private TelemetryListener listener;
    private int taskId = -1;

    public TelemetryManager(@NotNull McRPG plugin) {
        super(plugin);
    }

    /**
     * Initializes the telemetry system if enabled in config.
     * Registers the event listener and schedules the periodic snapshot task.
     */
    public void initialize() {
        if (!isEnabled()) {
            getPlugin().getLogger().info("Telemetry is disabled. No metrics will be collected.");
            return;
        }

        accumulator = new MetricsAccumulator(getPlugin());
        listener = new TelemetryListener(accumulator);

        // Register the event listener
        Bukkit.getPluginManager().registerEvents(listener, getPlugin());

        // Schedule the periodic snapshot task
        long frequencySeconds = getSnapshotFrequencySeconds();
        long frequencyTicks = frequencySeconds * 20L;

        taskId = Bukkit.getScheduler().runTaskTimer(getPlugin(), this::takeSnapshot, frequencyTicks, frequencyTicks).getTaskId();

        getPlugin().getLogger().info(String.format("Telemetry enabled. Snapshots every %d seconds.", frequencySeconds));
    }

    /**
     * Shuts down the telemetry system. Cancels the scheduled task and takes a final snapshot.
     */
    public void shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        if (accumulator != null) {
            // Take a final snapshot on shutdown
            takeSnapshot();
        }
    }

    /**
     * Takes a metrics snapshot and processes it.
     * <p>
     * Currently logs the snapshot summary. In the future, this will push the
     * snapshot to a remote telemetry backend via HTTPS.
     */
    private void takeSnapshot() {
        if (accumulator == null) {
            return;
        }

        MetricsSnapshot snapshot = accumulator.snapshotAndReset();
        logSnapshot(snapshot);
    }

    /**
     * Logs a human-readable summary of a metrics snapshot.
     */
    private void logSnapshot(@NotNull MetricsSnapshot snapshot) {
        getPlugin().getLogger().info("=== Telemetry Snapshot ===");
        getPlugin().getLogger().info(String.format("  Window: %dms | Online players: %d",
                snapshot.windowDurationMillis(), snapshot.onlinePlayerCount()));

        // Skill metrics
        if (!snapshot.skillMetrics().isEmpty()) {
            getPlugin().getLogger().info("  --- Skill Metrics ---");
            for (Map.Entry<NamespacedKey, SkillMetrics> entry : snapshot.skillMetrics().entrySet()) {
                SkillMetrics sm = entry.getValue();
                getPlugin().getLogger().info(String.format("    %s: xp=%d events=%d levelups=%d active_players=%d",
                        sm.skillKey().getKey(), sm.totalXpGained(), sm.xpGainEventCount(),
                        sm.levelUpCount(), sm.activePlayerCount()));
            }
        }

        // Ability metrics
        if (!snapshot.abilityMetrics().isEmpty()) {
            getPlugin().getLogger().info("  --- Ability Activations ---");
            for (Map.Entry<NamespacedKey, AbilityMetrics> entry : snapshot.abilityMetrics().entrySet()) {
                AbilityMetrics am = entry.getValue();
                getPlugin().getLogger().info(String.format("    %s: activations=%d",
                        am.abilityKey().getKey(), am.activationCount()));
            }
        }

        // Loadout metrics
        LoadoutSnapshot loadout = snapshot.loadoutSnapshot();
        if (loadout.totalLoadouts() > 0) {
            getPlugin().getLogger().info(String.format("  --- Loadout Snapshot (%d loadouts) ---", loadout.totalLoadouts()));
            for (Map.Entry<NamespacedKey, Integer> entry : loadout.abilityEquipCounts().entrySet()) {
                double pct = (entry.getValue() * 100.0) / loadout.totalLoadouts();
                getPlugin().getLogger().info(String.format("    %s: %d (%.1f%%)",
                        entry.getKey().getKey(), entry.getValue(), pct));
            }

            // Log top co-occurrence pairs (up to 10)
            if (!loadout.pairCoOccurrences().isEmpty()) {
                getPlugin().getLogger().info("  --- Top Co-Occurrence Pairs ---");
                loadout.pairCoOccurrences().entrySet().stream()
                        .sorted(Map.Entry.<AbilityPair, Integer>comparingByValue().reversed())
                        .limit(10)
                        .forEach(entry -> {
                            AbilityPair pair = entry.getKey();
                            getPlugin().getLogger().info(String.format("    %s + %s: %d",
                                    pair.first().getKey(), pair.second().getKey(), entry.getValue()));
                        });
            }
        }

        getPlugin().getLogger().info("=== End Snapshot ===");
    }

    /**
     * Gets the accumulator for direct access (e.g., for testing or the telemetry info command).
     *
     * @return The {@link MetricsAccumulator}, or null if telemetry is disabled
     */
    public MetricsAccumulator getAccumulator() {
        return accumulator;
    }

    /**
     * Checks if telemetry is enabled in the config.
     */
    public boolean isEnabled() {
        return getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG)
                .getBoolean(MainConfigFile.TELEMETRY_ENABLED);
    }

    /**
     * Gets the configured snapshot frequency in seconds, with a minimum of 300 (5 minutes).
     */
    private long getSnapshotFrequencySeconds() {
        int configured = getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG)
                .getInt(MainConfigFile.TELEMETRY_SNAPSHOT_FREQUENCY);
        return Math.max(300, configured);
    }
}
