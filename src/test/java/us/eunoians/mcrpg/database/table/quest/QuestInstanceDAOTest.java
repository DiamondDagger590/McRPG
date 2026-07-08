package us.eunoians.mcrpg.database.table.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestInstanceDAOTest extends McRPGBaseTest {

    @DisplayName("Given a quest instance, when saving full tree, then prepared statements are returned")
    @Test
    public void saveFullQuestTree_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        QuestInstance instance = new QuestInstance(
                new NamespacedKey("mcrpg", "test_quest"),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "single_player"),
                QuestState.IN_PROGRESS,
                null,
                Instant.now(),
                null,
                null,
                new ManualQuestSource(),
                null
        );

        List<PreparedStatement> statements = QuestInstanceDAO.saveFullQuestTree(mockConnection, instance);
        assertNotNull(statements);
        assertFalse(statements.isEmpty());
    }

    @DisplayName("Given a quest instance, when saving, then the connection is used to prepare statements")
    @Test
    public void saveFullQuestTree_usesConnection() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        QuestInstance instance = new QuestInstance(
                new NamespacedKey("mcrpg", "dao_test"),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "single_player"),
                QuestState.COMPLETED,
                null,
                Instant.ofEpochMilli(1000L),
                Instant.ofEpochMilli(2000L),
                null,
                new ManualQuestSource(),
                null
        );

        QuestInstanceDAO.saveFullQuestTree(mockConnection, instance);
        verify(mockConnection).prepareStatement(anyString());
    }

    @DisplayName("Given a valid scope_type key in the database, loadScopeType returns the key")
    @Test
    public void loadScopeType_validKey_returnsKey() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("scope_type")).thenReturn("mcrpg:single_player");

        Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, UUID.randomUUID());

        assertFalse(result.isEmpty(), "Expected a non-empty Optional for a valid scope_type key");
    }

    @DisplayName("Given no matching row, loadScopeType returns empty")
    @Test
    public void loadScopeType_noRow_returnsEmpty() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, UUID.randomUUID());

        assertTrue(result.isEmpty(), "Expected empty Optional when no row exists");
    }

    @DisplayName("Given no matching row, loadQuestInstance returns empty")
    @Test
    public void loadQuestInstance_noRow_returnsEmpty() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Optional<QuestInstance> result = QuestInstanceDAO.loadQuestInstance(conn, UUID.randomUUID());

        assertTrue(result.isEmpty(), "Expected empty Optional when no row exists");
    }
}
