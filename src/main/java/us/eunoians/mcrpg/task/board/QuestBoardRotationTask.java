package us.eunoians.mcrpg.task.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.QuestBoard;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.board.refresh.RefreshType;
import us.eunoians.mcrpg.quest.board.refresh.RefreshTypeRegistry;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;
import us.eunoians.mcrpg.quest.board.refresh.builtin.WeeklyRefreshType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Periodic task that checks whether any time-based refresh types should trigger
 * a board rotation.
 * <p>
 * Follows the {@code QuestSaveTask} pattern. Iterates all registered time-based
 * {@link RefreshType} instances and delegates the refresh decision to them.
 */
public final class QuestBoardRotationTask extends CancelableCoreTask {

    private static final Logger LOGGER = McRPG.getInstance().getLogger();
    private static final String DEFAULT_ROTATION_TIMEZONE = "UTC";
    private static final String DEFAULT_ROTATION_TIME = "00:00";

    private final LocalTime rotationLocalTime;
    private final ZoneId rotationZone;
    private final Map<NamespacedKey, Long> lastRefreshEpochs = new HashMap<>();
    private boolean seededFromCurrentBoardRotations = false;

    public QuestBoardRotationTask(@NotNull McRPG plugin,
                                  double taskDelay,
                                  double taskFrequency,
                                  @NotNull String rotationTime,
                                  @NotNull String timezone) {
        super(plugin, taskDelay, taskFrequency);
        this.rotationLocalTime = parseTimeOrDefault(rotationTime);
        this.rotationZone = parseZoneOrDefault(timezone);
    }

    @Override
    protected void onIntervalComplete() {
        if (!seededFromCurrentBoardRotations) {
            seedFromCurrentBoardRotations();
        }
        ZonedDateTime now = getPlugin().getTimeProvider().now().atZone(rotationZone);

        if (now.toLocalTime().isBefore(rotationLocalTime)) {
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

    private void seedFromCurrentBoardRotations() {
        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);
        QuestBoard board = boardManager.getDefaultBoard();
        seedEpoch(DailyRefreshType.KEY, board.getCurrentDailyRotation().orElse(null));
        seedEpoch(WeeklyRefreshType.KEY, board.getCurrentWeeklyRotation().orElse(null));
        seededFromCurrentBoardRotations = true;
    }

    private void seedEpoch(@NotNull NamespacedKey refreshTypeKey, BoardRotation rotation) {
        if (rotation != null) {
            lastRefreshEpochs.put(refreshTypeKey, rotation.getRotationEpoch());
        }
    }

    /**
     * Parses the configured rotation time (strict ISO {@code HH:mm}) once at construction, falling back
     * to {@link #DEFAULT_ROTATION_TIME} with a WARNING when the value is missing or malformed. This
     * mirrors {@link #parseZoneOrDefault} and prevents a bad {@code rotation.time} from throwing a
     * {@link java.time.format.DateTimeParseException} on every interval tick (a per-minute crash loop
     * with a board that never rotates).
     *
     * @param time the configured rotation time string
     * @return the parsed {@link LocalTime}, or the default on invalid input
     */
    @NotNull
    private LocalTime parseTimeOrDefault(String time) {
        String configuredTime = time;
        if (configuredTime == null || configuredTime.isBlank()) {
            configuredTime = DEFAULT_ROTATION_TIME;
        }
        try {
            return LocalTime.parse(configuredTime);
        } catch (Exception exception) {
            LOGGER.warning("[QuestBoard] Invalid rotation time '" + configuredTime
                    + "' configured in board.yml (expected 24-hour HH:mm, e.g. \"06:00\"). Falling back to "
                    + DEFAULT_ROTATION_TIME + ".");
            return LocalTime.parse(DEFAULT_ROTATION_TIME);
        }
    }

    @NotNull
    private ZoneId parseZoneOrDefault(String timezone) {
        String configuredTimezone = timezone;
        if (configuredTimezone == null || configuredTimezone.isBlank()) {
            configuredTimezone = DEFAULT_ROTATION_TIMEZONE;
        }
        try {
            return ZoneId.of(configuredTimezone);
        } catch (Exception exception) {
            LOGGER.warning("[QuestBoard] Invalid rotation timezone '" + configuredTimezone
                    + "' configured in board.yml. Falling back to " + DEFAULT_ROTATION_TIMEZONE + ".");
            return ZoneId.of(DEFAULT_ROTATION_TIMEZONE);
        }
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
