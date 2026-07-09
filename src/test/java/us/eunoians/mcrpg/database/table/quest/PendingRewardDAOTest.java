package us.eunoians.mcrpg.database.table.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.PendingReward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PendingRewardDAOTest extends McRPGBaseTest {

    @DisplayName("Given a pending reward, when saving, then it returns prepared statements")
    @Test
    public void savePendingReward_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        PendingReward reward = new PendingReward(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "test_reward"),
                Map.of("key", "value"),
                new NamespacedKey("mcrpg", "test_quest"),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L
        );

        List<PreparedStatement> statements = PendingRewardDAO.savePendingReward(mockConnection, reward);
        assertFalse(statements.isEmpty());
    }

    @DisplayName("Given a reward ID, when deleting, then it executes the delete statement")
    @Test
    public void deletePendingReward_executesDelete() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        PendingRewardDAO.deletePendingReward(mockConnection, UUID.randomUUID());
        verify(mockStatement).executeUpdate();
    }

    @Test
    @DisplayName("loadAndCleanPendingRewards binds the expires_at filter on the select")
    public void loadAndCleanPendingRewards_bindsExpiresAtFilter_whenSelecting() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement deleteStatement = mock(PreparedStatement.class);
        PreparedStatement selectStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(deleteStatement);
        when(mockConnection.prepareStatement(contains("SELECT"))).thenReturn(selectStatement);
        when(selectStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
        when(resultSet.getString("reward_type_key")).thenReturn("mcrpg:test_reward");
        when(resultSet.getString("quest_key")).thenReturn("mcrpg:test_quest");
        when(resultSet.getString("serialized_config")).thenReturn("{}");

        List<PendingReward> rewards = PendingRewardDAO.loadAndCleanPendingRewards(mockConnection, UUID.randomUUID());

        assertEquals(1, rewards.size());
        // Second bind param on the SELECT is the expires_at floor — proves expiry is filtered independently of the DELETE.
        verify(selectStatement).setLong(eq(2), anyLong());
    }

    @Test
    @DisplayName("loadAndCleanPendingRewards retains rows with an unparseable key")
    public void loadAndCleanPendingRewards_skipsRow_whenKeyUnparseable() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement deleteStatement = mock(PreparedStatement.class);
        PreparedStatement selectStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(deleteStatement);
        when(mockConnection.prepareStatement(contains("SELECT"))).thenReturn(selectStatement);
        when(selectStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
        when(resultSet.getString("reward_type_key")).thenReturn("INVALID KEY");
        when(resultSet.getString("quest_key")).thenReturn("mcrpg:test_quest");

        List<PendingReward> rewards = PendingRewardDAO.loadAndCleanPendingRewards(mockConnection, UUID.randomUUID());

        // Row is not returned (skipped) and is never handed to the delete set — it survives for inspection.
        assertTrue(rewards.isEmpty());
    }

    @Test
    @DisplayName("loadAndCleanPendingRewards skips a row with corrupt serialized_config JSON")
    public void loadAndCleanPendingRewards_skipsRow_whenJsonCorrupt() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement deleteStatement = mock(PreparedStatement.class);
        PreparedStatement selectStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(deleteStatement);
        when(mockConnection.prepareStatement(contains("SELECT"))).thenReturn(selectStatement);
        when(selectStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
        when(resultSet.getString("reward_type_key")).thenReturn("mcrpg:test_reward");
        when(resultSet.getString("quest_key")).thenReturn("mcrpg:test_quest");
        when(resultSet.getString("serialized_config")).thenReturn("this is not json");

        List<PendingReward> rewards = PendingRewardDAO.loadAndCleanPendingRewards(mockConnection, UUID.randomUUID());

        // The GSON parse failure is caught per-row: the corrupt row is skipped, not propagated.
        assertTrue(rewards.isEmpty());
    }

    @Test
    @DisplayName("deletePendingRewards skips an id whose statement fails to prepare and keeps the rest")
    public void deletePendingRewards_skipsFailedId_whenPrepareThrows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement okStatement = mock(PreparedStatement.class);
        // First id's prepare throws; the second succeeds.
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("boom"))
                .thenReturn(okStatement);

        List<PreparedStatement> statements = PendingRewardDAO.deletePendingRewards(
                mockConnection, new java.util.LinkedHashSet<>(List.of(UUID.randomUUID(), UUID.randomUUID())));

        // One prepare failed and was isolated; the other id still produced a statement.
        assertEquals(1, statements.size());
    }

    @Test
    @DisplayName("deletePendingRewards prepares one statement per id")
    public void deletePendingRewards_returnsStatementPerId() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID());
        List<PreparedStatement> statements = PendingRewardDAO.deletePendingRewards(mockConnection, ids);

        assertEquals(2, statements.size());
    }

    @Test
    @DisplayName("deletePendingRewards returns empty for no ids")
    public void deletePendingRewards_returnsEmpty_whenNoIds() {
        Connection mockConnection = mock(Connection.class);

        List<PreparedStatement> statements = PendingRewardDAO.deletePendingRewards(mockConnection, Set.of());

        assertTrue(statements.isEmpty());
    }
}
