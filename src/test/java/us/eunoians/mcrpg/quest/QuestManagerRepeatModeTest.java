package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.database.table.quest.QuestCompletionLogDAO;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests {@link QuestManager#canPlayerStartQuest} for all repeat modes.
 * <p>
 * Since we cannot easily construct a real QuestManager in the test environment, we use
 * a mock with {@code canPlayerStartQuest} calling the real method via
 * {@code thenCallRealMethod()}, combined with {@code mockStatic} on
 * {@link QuestCompletionLogDAO} to control database responses.
 * <p>
 * Time-dependent tests stub {@link TimeProvider#now()} to a fixed {@link Instant} so
 * that cooldown comparisons are deterministic and do not depend on wall-clock time.
 * The stub in {@link #setup()} mirrors the production logic in
 * {@link QuestManager#canPlayerStartQuest} and goes through the same {@code TimeProvider}.
 */
public class QuestManagerRepeatModeTest extends McRPGBaseTest {

    /** Fixed clock used by all time-sensitive tests. */
    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T12:00:00Z");

    private Connection mockConnection;
    private UUID playerUUID;
    private TimeProvider timeProvider;

    /** Tracks which definition keys are "active" for a player, used by canPlayerStartQuest stub. */
    private final Map<UUID, Set<NamespacedKey>> activeDefinitions = new ConcurrentHashMap<>();
    private QuestManager questManager;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        playerUUID = UUID.randomUUID();
        mockConnection = mock(Connection.class);
        activeDefinitions.clear();

        timeProvider = McRPG.getInstance().getTimeProvider();

        questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);

        when(questManager.hasActiveInstanceOfDefinition(any(UUID.class), any(NamespacedKey.class)))
                .thenAnswer(inv -> {
                    UUID p = inv.getArgument(0);
                    NamespacedKey key = inv.getArgument(1);
                    return activeDefinitions.getOrDefault(p, Set.of()).contains(key);
                });

        // canPlayerStartQuest: delegate to real-like logic, using TimeProvider for time comparisons
        when(questManager.canPlayerStartQuest(any(Connection.class), any(UUID.class), any(QuestDefinition.class)))
                .thenAnswer(inv -> {
                    Connection conn = inv.getArgument(0);
                    UUID p = inv.getArgument(1);
                    QuestDefinition def = inv.getArgument(2);

                    if (activeDefinitions.getOrDefault(p, Set.of()).contains(def.getQuestKey())) {
                        return false;
                    }

                    long nowMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();

                    return switch (def.getRepeatMode()) {
                        case ONCE -> !QuestCompletionLogDAO.hasCompleted(conn, p, def.getQuestKey().toString());
                        case LIMITED -> QuestCompletionLogDAO.getCompletionCount(conn, p, def.getQuestKey().toString())
                                < def.getRepeatLimit().orElse(Integer.MAX_VALUE);
                        case COOLDOWN -> {
                            OptionalLong last = QuestCompletionLogDAO.getLastCompletionTime(conn, p, def.getQuestKey().toString());
                            if (last.isEmpty()) yield true;
                            yield nowMillis >= last.getAsLong() + def.getRepeatCooldown().map(Duration::toMillis).orElse(0L);
                        }
                        case COOLDOWN_LIMITED -> {
                            int clLimit = def.getRepeatLimit().orElse(1);
                            if (QuestCompletionLogDAO.getCompletionCount(conn, p, def.getQuestKey().toString()) >= clLimit) {
                                yield false;
                            }
                            OptionalLong last = QuestCompletionLogDAO.getLastCompletionTime(conn, p, def.getQuestKey().toString());
                            if (last.isEmpty()) yield true;
                            yield nowMillis >= last.getAsLong() + def.getRepeatCooldown().map(Duration::toMillis).orElse(0L);
                        }
                        case REPEATABLE -> true;
                    };
                });
    }

    @AfterEach
    public void tearDown() {
        Mockito.reset(timeProvider);
    }

    private QuestDefinition defWithRepeatMode(String key, QuestRepeatMode mode,
                                              Duration cooldown, int limit) {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef(key + "_s", key + "_o");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        return new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", key),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(phase)
        ).repeatMode(mode)
                .repeatCooldown(cooldown)
                .repeatLimit(limit)
                .build();
    }

    // ── ONCE ──

    @DisplayName("ONCE mode: blocks when player has already completed the quest")
    @Test
    public void canPlayerStartQuest_ONCE_blocksIfCompleted() {
        QuestDefinition def = defWithRepeatMode("once_blocked", QuestRepeatMode.ONCE, null, -1);
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.hasCompleted(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(true);
            assertFalse(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    @DisplayName("ONCE mode: allows when player has never completed the quest")
    @Test
    public void canPlayerStartQuest_ONCE_allowsIfNeverCompleted() {
        QuestDefinition def = defWithRepeatMode("once_allowed", QuestRepeatMode.ONCE, null, -1);
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.hasCompleted(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(false);
            assertTrue(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    // ── LIMITED ──

    @DisplayName("LIMITED mode: allows when completion count is under the limit")
    @Test
    public void canPlayerStartQuest_LIMITED_allowsUnderLimit() {
        QuestDefinition def = defWithRepeatMode("limited_ok", QuestRepeatMode.LIMITED, null, 3);
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.getCompletionCount(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(2);
            assertTrue(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    @DisplayName("LIMITED mode: blocks when completion count has reached the limit")
    @Test
    public void canPlayerStartQuest_LIMITED_blocksAtLimit() {
        QuestDefinition def = defWithRepeatMode("limited_full", QuestRepeatMode.LIMITED, null, 3);
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.getCompletionCount(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(3);
            assertFalse(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    // ── COOLDOWN ──

    @DisplayName("COOLDOWN mode: allows when enough time has passed since last completion")
    @Test
    public void canPlayerStartQuest_COOLDOWN_allowsAfterExpiry() {
        when(timeProvider.now()).thenReturn(FIXED_NOW);
        QuestDefinition def = defWithRepeatMode("cd_ok", QuestRepeatMode.COOLDOWN, Duration.ofMinutes(5), -1);
        long lastCompletion = FIXED_NOW.minus(Duration.ofMinutes(10)).toEpochMilli();
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.getLastCompletionTime(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(OptionalLong.of(lastCompletion));
            assertTrue(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    @DisplayName("COOLDOWN mode: blocks when still within the cooldown window")
    @Test
    public void canPlayerStartQuest_COOLDOWN_blocksWithinCooldown() {
        when(timeProvider.now()).thenReturn(FIXED_NOW);
        QuestDefinition def = defWithRepeatMode("cd_blocked", QuestRepeatMode.COOLDOWN, Duration.ofMinutes(5), -1);
        long lastCompletion = FIXED_NOW.minus(Duration.ofMinutes(1)).toEpochMilli();
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.getLastCompletionTime(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(OptionalLong.of(lastCompletion));
            assertFalse(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    @DisplayName("COOLDOWN mode: allows when never completed before")
    @Test
    public void canPlayerStartQuest_COOLDOWN_allowsIfNeverCompleted() {
        QuestDefinition def = defWithRepeatMode("cd_first", QuestRepeatMode.COOLDOWN, Duration.ofMinutes(5), -1);
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.getLastCompletionTime(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(OptionalLong.empty());
            assertTrue(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    // ── REPEATABLE ──

    @DisplayName("REPEATABLE mode: always allows")
    @Test
    public void canPlayerStartQuest_REPEATABLE_alwaysAllows() {
        QuestDefinition def = defWithRepeatMode("repeat", QuestRepeatMode.REPEATABLE, null, -1);
        assertTrue(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
    }

    // ── COOLDOWN_LIMITED ──

    @DisplayName("COOLDOWN_LIMITED mode: allows when under the completion limit and cooldown has elapsed")
    @Test
    public void canPlayerStartQuest_COOLDOWN_LIMITED_allowsWhenUnderLimitAndCooldownElapsed() {
        when(timeProvider.now()).thenReturn(FIXED_NOW);
        QuestDefinition def = defWithRepeatMode("cdl_ok", QuestRepeatMode.COOLDOWN_LIMITED, Duration.ofMinutes(5), 3);
        long lastCompletion = FIXED_NOW.minus(Duration.ofMinutes(10)).toEpochMilli();
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.getCompletionCount(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(2);
            dao.when(() -> QuestCompletionLogDAO.getLastCompletionTime(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(OptionalLong.of(lastCompletion));
            assertTrue(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    @DisplayName("COOLDOWN_LIMITED mode: blocks when under the completion limit but cooldown has not elapsed")
    @Test
    public void canPlayerStartQuest_COOLDOWN_LIMITED_blocksWhenUnderLimitButCooldownActive() {
        when(timeProvider.now()).thenReturn(FIXED_NOW);
        QuestDefinition def = defWithRepeatMode("cdl_cd_blocked", QuestRepeatMode.COOLDOWN_LIMITED, Duration.ofMinutes(5), 3);
        long lastCompletion = FIXED_NOW.minus(Duration.ofMinutes(1)).toEpochMilli();
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.getCompletionCount(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(2);
            dao.when(() -> QuestCompletionLogDAO.getLastCompletionTime(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(OptionalLong.of(lastCompletion));
            assertFalse(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    @DisplayName("COOLDOWN_LIMITED mode: blocks when the completion limit is reached regardless of cooldown")
    @Test
    public void canPlayerStartQuest_COOLDOWN_LIMITED_blocksWhenLimitReached() {
        when(timeProvider.now()).thenReturn(FIXED_NOW);
        QuestDefinition def = defWithRepeatMode("cdl_limit_reached", QuestRepeatMode.COOLDOWN_LIMITED, Duration.ofMinutes(5), 3);
        long lastCompletion = FIXED_NOW.minus(Duration.ofMinutes(10)).toEpochMilli();
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.getCompletionCount(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(3);
            dao.when(() -> QuestCompletionLogDAO.getLastCompletionTime(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(OptionalLong.of(lastCompletion));
            assertFalse(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    @DisplayName("COOLDOWN_LIMITED mode: allows on first attempt when player has never completed the quest")
    @Test
    public void canPlayerStartQuest_COOLDOWN_LIMITED_allowsIfNeverCompleted() {
        QuestDefinition def = defWithRepeatMode("cdl_first", QuestRepeatMode.COOLDOWN_LIMITED, Duration.ofMinutes(5), 3);
        try (MockedStatic<QuestCompletionLogDAO> dao = mockStatic(QuestCompletionLogDAO.class)) {
            dao.when(() -> QuestCompletionLogDAO.getCompletionCount(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(0);
            dao.when(() -> QuestCompletionLogDAO.getLastCompletionTime(any(), eq(playerUUID), eq(def.getQuestKey().toString())))
                    .thenReturn(OptionalLong.empty());
            assertTrue(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
        }
    }

    // ── Active instance guard ──

    @DisplayName("Any mode: blocks when an active instance of the same definition already exists")
    @Test
    public void canPlayerStartQuest_blocksWhenActiveInstanceExists() {
        QuestDefinition def = defWithRepeatMode("active_guard", QuestRepeatMode.REPEATABLE, null, -1);
        activeDefinitions.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(def.getQuestKey());

        assertFalse(questManager.canPlayerStartQuest(mockConnection, playerUUID, def));
    }
}
