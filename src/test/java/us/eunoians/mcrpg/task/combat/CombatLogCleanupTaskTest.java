package us.eunoians.mcrpg.task.combat;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CombatLogCleanupTask")
class CombatLogCleanupTaskTest extends McRPGBaseTest {

    private YamlDocument combatConfig;
    private PreparedStatement statement;

    @BeforeEach
    void setUp() throws Exception {
        FileManager fileManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        combatConfig = mock(YamlDocument.class);
        lenient().when(fileManager.getFile(FileType.COMBAT_CONFIG)).thenReturn(combatConfig);

        Connection connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        lenient().when(connection.prepareStatement(anyString())).thenReturn(statement);
        lenient().when(statement.executeUpdate()).thenReturn(3);

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
    }

    private CombatLogCleanupTask taskWithRetention(int days) {
        lenient().when(combatConfig.getInt(CombatConfigFile.AUDIT_RETENTION_DAYS)).thenReturn(days);
        return new CombatLogCleanupTask(mcRPG, 86400);
    }

    @Test
    @DisplayName("runInitialCleanup deletes entries older than the configured retention period")
    void runInitialCleanup_deletesOlderEntries() throws SQLException {
        CombatLogCleanupTask task = taskWithRetention(30);

        task.runInitialCleanup();

        verify(statement).setLong(eq(1), anyLong());
        verify(statement).executeUpdate();
    }

    @Test
    @DisplayName("does not delete when retentionDays is 0")
    void doesNotDelete_whenRetentionZero() throws SQLException {
        CombatLogCleanupTask task = taskWithRetention(0);

        task.runInitialCleanup();

        verify(statement, never()).executeUpdate();
    }

    @Test
    @DisplayName("does not delete when retentionDays is negative")
    void doesNotDelete_whenRetentionNegative() throws SQLException {
        CombatLogCleanupTask task = taskWithRetention(-5);

        task.runInitialCleanup();

        verify(statement, never()).executeUpdate();
    }

    @Test
    @DisplayName("periodic onIntervalComplete uses the same cleanup logic as runInitialCleanup")
    void onIntervalComplete_usesSameCleanupLogic() throws SQLException {
        CombatLogCleanupTask task = taskWithRetention(30);

        task.onIntervalComplete();

        verify(statement).executeUpdate();
    }

    @Test
    @DisplayName("reload behavior: changing retentionDays and calling reloadContent updates the next run")
    void reload_updatesRetentionForNextRun() throws SQLException {
        CombatLogCleanupTask task = taskWithRetention(0);
        task.runInitialCleanup();
        verify(statement, never()).executeUpdate();

        when(combatConfig.getInt(CombatConfigFile.AUDIT_RETENTION_DAYS)).thenReturn(30);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.RELOADABLE_CONTENT)
                .reloadAllContent();

        task.runInitialCleanup();

        verify(statement).executeUpdate();
    }
}
