package us.eunoians.mcrpg.combat.log;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatTestSupport;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.event.combat.CombatLogPunishmentEvent;
import us.eunoians.mcrpg.event.combat.PlayerCombatLogEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CombatLogEnforcer")
class CombatLogEnforcerTest extends McRPGBaseTest {

    private YamlDocument combatConfig;
    private CombatTrackerManager combatTrackerManager;
    private CombatLogEnforcer enforcer;
    private PreparedStatement statement;

    @BeforeEach
    void setUp() throws Exception {
        combatConfig = CombatTestSupport.mockCombatConfig(mcRPG, 8.0, 16, 0.5);
        lenient().when(combatConfig.getString(CombatConfigFile.COMBAT_LOG_MODE, "DISABLED")).thenReturn("PLAYERS");
        lenient().when(combatConfig.getBoolean(CombatConfigFile.PUNISHMENT_KILL_ON_LOGOUT)).thenReturn(true);
        lenient().when(combatConfig.getBoolean(CombatConfigFile.PUNISHMENT_DROP_ITEMS)).thenReturn(true);
        lenient().when(combatConfig.getBoolean(CombatConfigFile.PUNISHMENT_BROADCAST_MESSAGE)).thenReturn(true);

        Connection connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        lenient().when(connection.prepareStatement(anyString())).thenReturn(statement);

        ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        lenient().when(executor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return CompletableFuture.completedFuture(null);
        });

        Database database = mock(Database.class);
        lenient().when(database.getConnection()).thenReturn(connection);
        lenient().when(database.getDatabaseExecutorService()).thenReturn(executor);

        McRPGDatabaseManager databaseManager = mock(McRPGDatabaseManager.class);
        lenient().when(databaseManager.getDatabase()).thenReturn(database);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER).register(databaseManager);

        mcRPG.registryAccess().registry(RegistryKey.MANAGER).register(new ReloadableContentManager(mcRPG));

        combatTrackerManager = new CombatTrackerManager(mcRPG);
        enforcer = new CombatLogEnforcer(mcRPG);
    }

    @AfterEach
    void cleanUp() {
        PlayerCombatLogEvent.getHandlerList().unregister(mcRPG);
        CombatLogPunishmentEvent.getHandlerList().unregister(mcRPG);
    }

    private CombatSession pvpSession(PlayerMock player) {
        PlayerMock other = server.addPlayer();
        combatTrackerManager.handleCombatInteraction(player.getUniqueId(), other.getUniqueId(),
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("PLAYER"));
        return combatTrackerManager.getSession(player.getUniqueId()).orElseThrow();
    }

    private CombatSession pveSession(PlayerMock player) {
        combatTrackerManager.handleCombatInteraction(player.getUniqueId(), UUID.randomUUID(),
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
        return combatTrackerManager.getSession(player.getUniqueId()).orElseThrow();
    }

    @Nested
    @DisplayName("Reloadable initialization")
    class ReloadableInitialization {

        @Test
        @DisplayName("getMode returns the cached mode parsed from configuration")
        void getMode_returnsCachedMode() {
            assertEquals(CombatLogMode.PLAYERS, enforcer.getMode().getContent());
        }

        @Test
        @DisplayName("falls back to DISABLED on an unrecognized mode string")
        void unrecognizedModeString_fallsBackToDisabled() {
            lenient().when(combatConfig.getString(CombatConfigFile.COMBAT_LOG_MODE, "DISABLED")).thenReturn("GARBAGE");

            CombatLogEnforcer garbageEnforcer = new CombatLogEnforcer(mcRPG);

            assertEquals(CombatLogMode.DISABLED, garbageEnforcer.getMode().getContent());
        }
    }

    @Nested
    @DisplayName("Mode evaluation")
    class ModeEvaluation {

        @Test
        @DisplayName("does not fire events when mode is DISABLED")
        void doesNotFireEvents_whenModeDisabled() {
            lenient().when(combatConfig.getString(CombatConfigFile.COMBAT_LOG_MODE, "DISABLED")).thenReturn("DISABLED");
            CombatLogEnforcer disabledEnforcer = new CombatLogEnforcer(mcRPG);

            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);

            List<PlayerCombatLogEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onLog(PlayerCombatLogEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            disabledEnforcer.evaluateAndEnforce(player, session);

            assertTrue(captured.isEmpty());
        }

        @Test
        @DisplayName("does not fire events when mode is PLAYERS and session type is PVE")
        void doesNotFireEvents_whenPlayersModeAndPve() {
            PlayerMock player = server.addPlayer();
            CombatSession session = pveSession(player);

            List<PlayerCombatLogEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onLog(PlayerCombatLogEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            enforcer.evaluateAndEnforce(player, session);

            assertTrue(captured.isEmpty());
        }

        @Test
        @DisplayName("fires events when mode is PLAYERS and session type is PVP")
        void firesEvents_whenPlayersModeAndPvp() {
            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);

            List<PlayerCombatLogEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onLog(PlayerCombatLogEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            enforcer.evaluateAndEnforce(player, session);

            assertEquals(1, captured.size());
        }

        @Test
        @DisplayName("fires events when mode is MOBS_AND_PLAYERS and session type is PVE")
        void firesEvents_whenMobsAndPlayersModeAndPve() {
            lenient().when(combatConfig.getString(CombatConfigFile.COMBAT_LOG_MODE, "DISABLED")).thenReturn("MOBS_AND_PLAYERS");
            CombatLogEnforcer mobsEnforcer = new CombatLogEnforcer(mcRPG);

            PlayerMock player = server.addPlayer();
            CombatSession session = pveSession(player);

            List<PlayerCombatLogEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onLog(PlayerCombatLogEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            mobsEnforcer.evaluateAndEnforce(player, session);

            assertEquals(1, captured.size());
        }
    }

    @Nested
    @DisplayName("Detection event")
    class DetectionEvent {

        @Test
        @DisplayName("fires PlayerCombatLogEvent with correct player, session, combat type, and participants")
        void firesDetectionEvent_withCorrectData() {
            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);

            List<PlayerCombatLogEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onLog(PlayerCombatLogEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            enforcer.evaluateAndEnforce(player, session);

            assertEquals(1, captured.size());
            PlayerCombatLogEvent event = captured.get(0);
            assertSame(player, event.getPlayer());
            assertSame(session, event.getSession());
            assertEquals(session.getCombatType(), event.getCombatType());
            assertEquals(1, event.getParticipants().size());
        }

        @Test
        @DisplayName("does not proceed to punishment when applyPunishment is set to false")
        void doesNotProceed_whenApplyPunishmentSetFalse() {
            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);
            player.setHealth(20.0);

            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onLog(PlayerCombatLogEvent event) {
                    event.setApplyPunishment(false);
                }
            }, mcRPG);

            enforcer.evaluateAndEnforce(player, session);

            assertEquals(20.0, player.getHealth());
        }
    }

    @Nested
    @DisplayName("Punishment event and application")
    class PunishmentApplication {

        @Test
        @DisplayName("fires CombatLogPunishmentEvent with punishment map populated from cached config")
        void firesPunishmentEvent_populatedFromConfig() {
            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);

            List<CombatLogPunishmentEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPunishment(CombatLogPunishmentEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            enforcer.evaluateAndEnforce(player, session);

            assertEquals(1, captured.size());
            assertTrue(captured.get(0).isPunishmentEnabled(CombatLogPunishmentType.KILL_ON_LOGOUT));
            assertTrue(captured.get(0).isPunishmentEnabled(CombatLogPunishmentType.DROP_ITEMS));
            assertTrue(captured.get(0).isPunishmentEnabled(CombatLogPunishmentType.BROADCAST_MESSAGE));
        }

        @Test
        @DisplayName("does not apply punishments when all are disabled by listeners")
        void doesNotApplyPunishments_whenAllDisabled() {
            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);
            player.setHealth(20.0);

            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPunishment(CombatLogPunishmentEvent event) {
                    for (CombatLogPunishmentType type : event.getEnabledPunishments()) {
                        event.setPunishmentEnabled(type, false);
                    }
                }
            }, mcRPG);

            enforcer.evaluateAndEnforce(player, session);

            assertEquals(20.0, player.getHealth());
        }

        @Test
        @DisplayName("KILL_ON_LOGOUT sets player health to zero")
        void killOnLogout_setsHealthToZero() {
            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);
            player.setHealth(20.0);

            enforcer.evaluateAndEnforce(player, session);

            assertEquals(0.0, player.getHealth());
        }

        @Test
        @DisplayName("mutual exclusion: an excluded type's apply() is not called when the excluding type is enabled")
        void mutualExclusion_excludedTypeNotApplied() {
            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);

            boolean[] excludingApplied = {false};
            boolean[] excludedApplied = {false};
            CombatLogPunishmentType excludedType = new CombatLogPunishmentType(
                    new NamespacedKey("thirdparty", "excluded_type"), "excluded", null) {
                @Override
                public void apply(org.bukkit.entity.Player p, CombatSession s, us.eunoians.mcrpg.McRPG plugin) {
                    excludedApplied[0] = true;
                }
            };
            CombatLogPunishmentType excludingType = new CombatLogPunishmentType(
                    new NamespacedKey("thirdparty", "excluding_type"), "excluding", null) {
                @Override
                @org.jetbrains.annotations.NotNull
                public java.util.Set<NamespacedKey> getExcludes() {
                    return java.util.Set.of(excludedType.getKey());
                }

                @Override
                public void apply(org.bukkit.entity.Player p, CombatSession s, us.eunoians.mcrpg.McRPG plugin) {
                    excludingApplied[0] = true;
                }
            };

            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPunishment(CombatLogPunishmentEvent event) {
                    event.setPunishmentEnabled(excludedType, true);
                    event.setPunishmentEnabled(excludingType, true);
                }
            }, mcRPG);

            enforcer.evaluateAndEnforce(player, session);

            assertTrue(excludingApplied[0], "The excluding type should still be applied");
            assertFalse(excludedApplied[0], "The excluded type must not be applied when its excluder is enabled");
        }

        @Test
        @DisplayName("audit recording submits an async DAO insert")
        void auditRecording_submitsAsyncInsert() throws Exception {
            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);

            enforcer.evaluateAndEnforce(player, session);

            verify(statement).setString(1, player.getUniqueId().toString());
            verify(statement).executeUpdate();
        }
    }

    @Nested
    @DisplayName("Third-party type integration")
    class ThirdPartyTypeIntegration {

        @Test
        @DisplayName("a custom type added via setPunishmentEnabled has its apply() called")
        void customType_appliedWhenAddedToEvent() {
            PlayerMock player = server.addPlayer();
            CombatSession session = pvpSession(player);

            boolean[] applied = {false};
            CombatLogPunishmentType customType = new CombatLogPunishmentType(
                    new NamespacedKey("thirdparty", "custom_punishment"), "custom", null) {
                @Override
                public void apply(org.bukkit.entity.Player p, CombatSession s, us.eunoians.mcrpg.McRPG plugin) {
                    applied[0] = true;
                }
            };

            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPunishment(CombatLogPunishmentEvent event) {
                    event.setPunishmentEnabled(customType, true);
                }
            }, mcRPG);

            enforcer.evaluateAndEnforce(player, session);

            assertTrue(applied[0]);
        }
    }
}
