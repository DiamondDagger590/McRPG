package us.eunoians.mcrpg.task.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.board.refresh.RefreshType;
import us.eunoians.mcrpg.quest.board.refresh.RefreshTypeRegistry;
import us.eunoians.mcrpg.quest.board.refresh.builtin.WeeklyRefreshType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Periodic task that checks whether any time-based refresh types should trigger
 * a board rotation.
 * <p>
 * Follows the {@code QuestSaveTask} pattern. Iterates all registered time-based
 * {@link RefreshType} instances and delegates the refresh decision to them.
 */
public final class QuestBoardRotationTask extends CancelableCoreTask {

    private final String rotationTime;
    private final String timezone;
    private final Map<NamespacedKey, Long> lastRefreshEpochs = new HashMap<>();

    public QuestBoardRotationTask(@NotNull McRPG plugin,
                                  double taskDelay,
                                  double taskFrequency,
                                  @NotNull String rotationTime,
                                  @NotNull String timezone) {
        super(plugin, taskDelay, taskFrequency);
        this.rotationTime = rotationTime;
        this.timezone = timezone;
    }

    @Override
    protected void onIntervalComplete() {
        ZoneId zone = ZoneId.of(timezone);
        ZonedDateTime now = getPlugin().getTimeProvider().now().atZone(zone);
        LocalTime configuredTime = LocalTime.parse(rotationTime);

        if (now.toLocalTime().isBefore(configuredTime)) {
            return;
        }

        RefreshTypeRegistry refreshRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.REFRESH_TYPE);
        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);

        for (RefreshType type : refreshRegistry.getTimeBasedTypes()) {
            long lastEpoch = lastRefreshEpochs.getOrDefault(type.getKey(), 0L);
            if (type.shouldRefresh(lastEpoch, now)) {
                boardManager.triggerRotation(type.getKey());
                lastRefreshEpochs.put(type.getKey(), computeCurrentEpoch(type, now));
            }
        }
    }

    private long computeCurrentEpoch(@NotNull RefreshType type, @NotNull ZonedDateTime now) {
        if (type.getKey().getKey().equals("daily")) {
            return now.toLocalDate().toEpochDay();
        }
        return WeeklyRefreshType.computeEpoch(now);
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected void onDelayComplete() {
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
}
