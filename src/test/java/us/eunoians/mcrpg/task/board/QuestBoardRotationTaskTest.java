package us.eunoians.mcrpg.task.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.QuestBoard;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.board.refresh.RefreshType;
import us.eunoians.mcrpg.quest.board.refresh.RefreshTypeRegistry;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;
import us.eunoians.mcrpg.quest.board.refresh.builtin.WeeklyRefreshType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestBoardRotationTaskTest extends McRPGBaseTest {

    private QuestBoardManager mockBoardManager;
    private QuestBoard mockBoard;
    private RefreshTypeRegistry refreshTypeRegistry;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        refreshTypeRegistry = new RefreshTypeRegistry();
        RegistryAccess.registryAccess().register(refreshTypeRegistry);
        mockBoardManager = mock(QuestBoardManager.class);
        mockBoard = mock(QuestBoard.class);
        when(mockBoardManager.getDefaultBoard()).thenReturn(mockBoard);
        when(mockBoard.getCurrentDailyRotation()).thenReturn(Optional.empty());
        when(mockBoard.getCurrentWeeklyRotation()).thenReturn(Optional.empty());
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockBoardManager);
        timeProvider = McRPG.getInstance().getTimeProvider();
    }

    private Instant instantAt(int hour, int minute) {
        return ZonedDateTime.of(2026, 2, 23, hour, minute, 0, 0, ZoneOffset.UTC).toInstant();
    }

    @DisplayName("When current time is after rotation time and shouldRefresh is true, triggerRotation is called")
    @Test
    void onIntervalComplete_triggersRotation_whenAfterRotationTimeAndShouldRefreshTrue() {
        DailyRefreshType daily = new DailyRefreshType();
        refreshTypeRegistry.register(daily);

        Instant instant = instantAt(14, 0);
        when(timeProvider.now()).thenReturn(instant);

        QuestBoardRotationTask task = spy(new QuestBoardRotationTask(
                McRPG.getInstance(), 0, 1f, "12:00", "UTC"));
        task.runTask();

        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(mockBoardManager, times(1)).triggerRotation(daily.getKey());
        verify(task, times(1)).onIntervalComplete();
    }

    @DisplayName("When current time is before rotation time, triggerRotation is not called")
    @Test
    void onIntervalComplete_doesNotTrigger_whenBeforeRotationTime() {
        DailyRefreshType daily = new DailyRefreshType();
        refreshTypeRegistry.register(daily);

        Instant instant = instantAt(6, 0);
        when(timeProvider.now()).thenReturn(instant);

        QuestBoardRotationTask task = spy(new QuestBoardRotationTask(
                McRPG.getInstance(), 0, 1f, "12:00", "UTC"));
        task.runTask();

        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(mockBoardManager, never()).triggerRotation(any());
        verify(task, times(1)).onIntervalComplete();
    }

    @DisplayName("When shouldRefresh returns false, triggerRotation is not called")
    @Test
    void onIntervalComplete_doesNotTrigger_whenShouldRefreshFalse() {
        RefreshType neverRefresh = new RefreshType(new NamespacedKey("mcrpg", "never_refresh")) {
            @Override
            public boolean isTimeBased() {
                return true;
            }

            @Override
            public boolean shouldRefresh(long lastRefreshEpoch, @NotNull ZonedDateTime now) {
                return false;
            }
        };
        refreshTypeRegistry.register(neverRefresh);

        Instant instant = instantAt(14, 0);
        when(timeProvider.now()).thenReturn(instant);

        QuestBoardRotationTask task = spy(new QuestBoardRotationTask(
                McRPG.getInstance(), 0, 1f, "12:00", "UTC"));
        task.runTask();

        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(mockBoardManager, never()).triggerRotation(any());
        verify(task, times(1)).onIntervalComplete();
    }

    @DisplayName("After triggering, same epoch does not re-trigger on next interval")
    @Test
    void onIntervalComplete_doesNotReTrigger_afterEpochUpdated() {
        DailyRefreshType daily = new DailyRefreshType();
        refreshTypeRegistry.register(daily);

        Instant instant = instantAt(14, 0);
        when(timeProvider.now()).thenReturn(instant);

        QuestBoardRotationTask task = spy(new QuestBoardRotationTask(
                McRPG.getInstance(), 0, 1f, "12:00", "UTC"));
        task.runTask();

        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(mockBoardManager, times(1)).triggerRotation(daily.getKey());
        verify(task, times(2)).onIntervalComplete();
    }

    @DisplayName("Multiple refresh types are each checked independently")
    @Test
    void onIntervalComplete_checksMultipleRefreshTypes() {
        DailyRefreshType daily = new DailyRefreshType();
        RefreshType alwaysRefresh = new RefreshType(new NamespacedKey("mcrpg", "always_refresh")) {
            @Override
            public boolean isTimeBased() {
                return true;
            }

            @Override
            public boolean shouldRefresh(long lastRefreshEpoch, @NotNull ZonedDateTime now) {
                return true;
            }
        };
        refreshTypeRegistry.register(daily);
        refreshTypeRegistry.register(alwaysRefresh);

        Instant instant = instantAt(14, 0);
        when(timeProvider.now()).thenReturn(instant);

        QuestBoardRotationTask task = spy(new QuestBoardRotationTask(
                McRPG.getInstance(), 0, 1f, "12:00", "UTC"));
        task.runTask();

        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(mockBoardManager, times(1)).triggerRotation(daily.getKey());
        verify(mockBoardManager, times(1)).triggerRotation(alwaysRefresh.getKey());
    }

    @DisplayName("Startup task seeds current board epochs and skips duplicate daily/weekly rotations")
    @Test
    void onIntervalComplete_doesNotRotate_whenCurrentEpochsAlreadyLoadedFromBoard() {
        DailyRefreshType daily = new DailyRefreshType();
        WeeklyRefreshType weekly = new WeeklyRefreshType(java.time.DayOfWeek.MONDAY);
        refreshTypeRegistry.register(daily);
        refreshTypeRegistry.register(weekly);

        Instant instant = instantAt(14, 0);
        when(timeProvider.now()).thenReturn(instant);
        ZonedDateTime now = instant.atZone(ZoneOffset.UTC);

        BoardRotation seededDailyRotation = new BoardRotation(
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "default_board"),
                daily.getKey(),
                now.toLocalDate().toEpochDay(),
                instant.toEpochMilli(),
                instant.plusSeconds(10).toEpochMilli()
        );
        BoardRotation seededWeeklyRotation = new BoardRotation(
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "default_board"),
                weekly.getKey(),
                WeeklyRefreshType.computeEpoch(now),
                instant.toEpochMilli(),
                instant.plusSeconds(10).toEpochMilli()
        );
        when(mockBoard.getCurrentDailyRotation()).thenReturn(Optional.of(seededDailyRotation));
        when(mockBoard.getCurrentWeeklyRotation()).thenReturn(Optional.of(seededWeeklyRotation));

        QuestBoardRotationTask task = spy(new QuestBoardRotationTask(
                McRPG.getInstance(), 0, 1f, "12:00", "UTC"));
        task.runTask();

        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(mockBoardManager, never()).triggerRotation(daily.getKey());
        verify(mockBoardManager, never()).triggerRotation(weekly.getKey());
        verify(task, times(1)).onIntervalComplete();
    }

    @DisplayName("Invalid timezone falls back to UTC and still triggers expected rotation checks")
    @Test
    void onIntervalComplete_usesUtcFallback_whenTimezoneIsInvalid() {
        DailyRefreshType daily = new DailyRefreshType();
        refreshTypeRegistry.register(daily);

        Instant instant = instantAt(14, 0);
        when(timeProvider.now()).thenReturn(instant);

        QuestBoardRotationTask task = spy(new QuestBoardRotationTask(
                McRPG.getInstance(), 0, 1f, "12:00", "definitely/not-a-timezone"));
        task.runTask();

        MockBukkit.getMock().getScheduler().performOneTick();
        instant = instant.plusSeconds(1);
        when(timeProvider.now()).thenReturn(instant);
        MockBukkit.getMock().getScheduler().performOneTick();

        verify(mockBoardManager, times(1)).triggerRotation(daily.getKey());
        verify(task, times(1)).onIntervalComplete();
    }
}
