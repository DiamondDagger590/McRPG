package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.PendingReward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PendingRewardDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("returns false when table already exists")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, PendingRewardDAO.TABLE_NAME)).thenReturn(true);

            boolean result = PendingRewardDAO.attemptCreateTable(conn, db);

            assertFalse(result);
        }

        @Test
        @DisplayName("returns true when table does not exist")
        void attemptCreateTable_returnsTrue_whenTableMissing() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(db.tableExists(conn, PendingRewardDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            boolean result = PendingRewardDAO.attemptCreateTable(conn, db);

            assertTrue(result);
            verify(ps).executeUpdate();
        }

        @Test
        @DisplayName("returns false when SQL exception is thrown during creation")
        void attemptCreateTable_returnsFalse_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, PendingRewardDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            boolean result = PendingRewardDAO.attemptCreateTable(conn, db);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("skips migration when version is current")
        void updateTable_skips_whenVersionCurrent() {
            Connection conn = mock(Connection.class);
            try (MockedStatic<TableVersionHistoryDAO> tvh = mockStatic(TableVersionHistoryDAO.class)) {
                tvh.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, PendingRewardDAO.TABLE_NAME)).thenReturn(1);

                PendingRewardDAO.updateTable(conn);

                tvh.verify(() -> TableVersionHistoryDAO.setTableVersion(eq(conn), anyString(), anyInt()), never());
            }
        }

        @Test
        @DisplayName("creates indexes when version is 0")
        void updateTable_createsIndexes_whenVersion0() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            try (MockedStatic<TableVersionHistoryDAO> tvh = mockStatic(TableVersionHistoryDAO.class)) {
                tvh.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, PendingRewardDAO.TABLE_NAME)).thenReturn(0);

                PendingRewardDAO.updateTable(conn);

                verify(ps, org.mockito.Mockito.times(2)).executeUpdate();
                tvh.verify(() -> TableVersionHistoryDAO.setTableVersion(conn, PendingRewardDAO.TABLE_NAME, 1));
            }
        }
    }

    @Nested
    @DisplayName("savePendingReward")
    class SavePendingReward {

        @Test
        @DisplayName("returns a prepared statement for a valid reward")
        void savePendingReward_returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            UUID rewardId = UUID.randomUUID();
            PendingReward reward = new PendingReward(
                    rewardId,
                    PLAYER_UUID,
                    new NamespacedKey("mcrpg", "test_reward"),
                    Map.of("key", "value"),
                    new NamespacedKey("mcrpg", "test_quest"),
                    1000L,
                    2000L
            );

            List<PreparedStatement> statements = PendingRewardDAO.savePendingReward(conn, reward);

            assertFalse(statements.isEmpty());
        }

        @Test
        @DisplayName("binds all parameters in correct order")
        void savePendingReward_bindsParametersCorrectly() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            UUID rewardId = UUID.randomUUID();
            NamespacedKey rewardTypeKey = new NamespacedKey("mcrpg", "xp_reward");
            NamespacedKey questKey = new NamespacedKey("mcrpg", "daily_mining");
            PendingReward reward = new PendingReward(
                    rewardId,
                    PLAYER_UUID,
                    rewardTypeKey,
                    Map.of("amount", "100"),
                    questKey,
                    5000L,
                    10000L
            );

            PendingRewardDAO.savePendingReward(conn, reward);

            verify(ps).setString(1, rewardId.toString());
            verify(ps).setString(2, PLAYER_UUID.toString());
            verify(ps).setString(3, rewardTypeKey.toString());
            verify(ps).setString(5, questKey.toString());
            verify(ps).setLong(6, 5000L);
            verify(ps).setLong(7, 10000L);
        }

        @Test
        @DisplayName("returns empty list when prepare throws")
        void savePendingReward_returnsEmpty_whenPrepareThrows() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            PendingReward reward = new PendingReward(
                    UUID.randomUUID(),
                    PLAYER_UUID,
                    new NamespacedKey("mcrpg", "test_reward"),
                    Map.of(),
                    new NamespacedKey("mcrpg", "test_quest"),
                    1000L,
                    2000L
            );

            List<PreparedStatement> statements = PendingRewardDAO.savePendingReward(conn, reward);

            assertTrue(statements.isEmpty());
        }
    }

    @Nested
    @DisplayName("loadAndCleanPendingRewards")
    class LoadAndCleanPendingRewards {

        @Test
        @DisplayName("returns rewards from valid rows")
        void loadAndCleanPendingRewards_returnsRewards() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement deleteStatement = mock(PreparedStatement.class);
            PreparedStatement selectStatement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(deleteStatement);
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(selectStatement);
            when(selectStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
            when(resultSet.getString("reward_type_key")).thenReturn("mcrpg:test_reward");
            when(resultSet.getString("quest_key")).thenReturn("mcrpg:test_quest");
            when(resultSet.getString("serialized_config")).thenReturn("{}");
            when(resultSet.getLong("created_at")).thenReturn(1000L);
            when(resultSet.getLong("expires_at")).thenReturn(99999L);

            List<PendingReward> rewards = PendingRewardDAO.loadAndCleanPendingRewards(conn, PLAYER_UUID);

            assertEquals(1, rewards.size());
            verify(deleteStatement).executeUpdate();
        }

        @Test
        @DisplayName("binds the expires_at filter on the select")
        void loadAndCleanPendingRewards_bindsExpiresAtFilter_whenSelecting() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement deleteStatement = mock(PreparedStatement.class);
            PreparedStatement selectStatement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(deleteStatement);
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(selectStatement);
            when(selectStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
            when(resultSet.getString("reward_type_key")).thenReturn("mcrpg:test_reward");
            when(resultSet.getString("quest_key")).thenReturn("mcrpg:test_quest");
            when(resultSet.getString("serialized_config")).thenReturn("{}");

            PendingRewardDAO.loadAndCleanPendingRewards(conn, PLAYER_UUID);

            verify(selectStatement).setLong(eq(2), anyLong());
        }

        @Test
        @DisplayName("skips rows with unparseable key")
        void loadAndCleanPendingRewards_skipsRow_whenKeyUnparseable() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement deleteStatement = mock(PreparedStatement.class);
            PreparedStatement selectStatement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(deleteStatement);
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(selectStatement);
            when(selectStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
            when(resultSet.getString("reward_type_key")).thenReturn("INVALID KEY");
            when(resultSet.getString("quest_key")).thenReturn("mcrpg:test_quest");

            List<PendingReward> rewards = PendingRewardDAO.loadAndCleanPendingRewards(conn, PLAYER_UUID);

            assertTrue(rewards.isEmpty());
        }

        @Test
        @DisplayName("skips rows with corrupt serialized_config JSON")
        void loadAndCleanPendingRewards_skipsRow_whenJsonCorrupt() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement deleteStatement = mock(PreparedStatement.class);
            PreparedStatement selectStatement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(deleteStatement);
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(selectStatement);
            when(selectStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
            when(resultSet.getString("reward_type_key")).thenReturn("mcrpg:test_reward");
            when(resultSet.getString("quest_key")).thenReturn("mcrpg:test_quest");
            when(resultSet.getString("serialized_config")).thenReturn("this is not json");

            List<PendingReward> rewards = PendingRewardDAO.loadAndCleanPendingRewards(conn, PLAYER_UUID);

            assertTrue(rewards.isEmpty());
        }

        @Test
        @DisplayName("returns empty list when SELECT throws SQL exception")
        void loadAndCleanPendingRewards_returnsEmpty_whenSelectThrowsSqlException() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement deleteStatement = mock(PreparedStatement.class);
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(deleteStatement);
            when(conn.prepareStatement(contains("SELECT"))).thenThrow(new SQLException("select error"));

            List<PendingReward> rewards = PendingRewardDAO.loadAndCleanPendingRewards(conn, PLAYER_UUID);

            assertTrue(rewards.isEmpty());
            verify(deleteStatement).executeUpdate();
        }
    }

    @Nested
    @DisplayName("listPendingRewards")
    class ListPendingRewards {

        @Test
        @DisplayName("returns rewards from valid rows")
        void listPendingRewards_returnsRewards_whenValidRows() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            UUID rewardId = UUID.randomUUID();
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("id")).thenReturn(rewardId.toString());
            when(rs.getString("reward_type_key")).thenReturn("mcrpg:xp_reward");
            when(rs.getString("quest_key")).thenReturn("mcrpg:daily_mining");
            when(rs.getString("serialized_config")).thenReturn("{\"amount\":100}");
            when(rs.getLong("created_at")).thenReturn(5000L);
            when(rs.getLong("expires_at")).thenReturn(10000L);

            List<PendingReward> result = PendingRewardDAO.listPendingRewards(conn, PLAYER_UUID);

            assertEquals(1, result.size());
            assertEquals(rewardId, result.get(0).getId());
            assertEquals("mcrpg:xp_reward", result.get(0).getRewardTypeKey().toString());
            assertEquals("mcrpg:daily_mining", result.get(0).getQuestKey().toString());
        }

        @Test
        @DisplayName("returns empty list when no rows")
        void listPendingRewards_returnsEmpty_whenNoRows() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            List<PendingReward> result = PendingRewardDAO.listPendingRewards(conn, PLAYER_UUID);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("skips rows with unparseable keys")
        void listPendingRewards_skipsRow_whenKeyUnparseable() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("id")).thenReturn(UUID.randomUUID().toString());
            when(rs.getString("reward_type_key")).thenReturn("BAD KEY");
            when(rs.getString("quest_key")).thenReturn("mcrpg:test");

            List<PendingReward> result = PendingRewardDAO.listPendingRewards(conn, PLAYER_UUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("skips rows with corrupt JSON")
        void listPendingRewards_skipsRow_whenJsonCorrupt() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("id")).thenReturn(UUID.randomUUID().toString());
            when(rs.getString("reward_type_key")).thenReturn("mcrpg:test_reward");
            when(rs.getString("quest_key")).thenReturn("mcrpg:test_quest");
            when(rs.getString("serialized_config")).thenReturn("not json at all");

            List<PendingReward> result = PendingRewardDAO.listPendingRewards(conn, PLAYER_UUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list when SQL exception is thrown")
        void listPendingRewards_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            List<PendingReward> result = PendingRewardDAO.listPendingRewards(conn, PLAYER_UUID);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteAllForPlayer")
    class DeleteAllForPlayer {

        @Test
        @DisplayName("returns deleted row count")
        void deleteAllForPlayer_returnsDeletedCount() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(3);

            int result = PendingRewardDAO.deleteAllForPlayer(conn, PLAYER_UUID);

            assertEquals(3, result);
            verify(ps).setString(1, PLAYER_UUID.toString());
        }

        @Test
        @DisplayName("returns 0 when SQL exception is thrown")
        void deleteAllForPlayer_returnsZero_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            int result = PendingRewardDAO.deleteAllForPlayer(conn, PLAYER_UUID);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("deletePendingReward")
    class DeletePendingReward {

        @Test
        @DisplayName("executes delete statement")
        void deletePendingReward_executesDelete() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            UUID rewardId = UUID.randomUUID();
            PendingRewardDAO.deletePendingReward(conn, rewardId);

            verify(ps).setString(1, rewardId.toString());
            verify(ps).executeUpdate();
        }
    }

    @Nested
    @DisplayName("deletePendingRewards")
    class DeletePendingRewards {

        @Test
        @DisplayName("returns one statement per id")
        void deletePendingRewards_returnsStatementPerId() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID());
            List<PreparedStatement> statements = PendingRewardDAO.deletePendingRewards(conn, ids);

            assertEquals(2, statements.size());
        }

        @Test
        @DisplayName("returns empty for no ids")
        void deletePendingRewards_returnsEmpty_whenNoIds() {
            Connection conn = mock(Connection.class);

            List<PreparedStatement> statements = PendingRewardDAO.deletePendingRewards(conn, Set.of());

            assertTrue(statements.isEmpty());
        }

        @Test
        @DisplayName("skips failed id when prepare throws and keeps the rest")
        void deletePendingRewards_skipsFailedId_whenPrepareThrows() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement okStatement = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString()))
                    .thenThrow(new SQLException("boom"))
                    .thenReturn(okStatement);

            List<PreparedStatement> statements = PendingRewardDAO.deletePendingRewards(
                    conn, new LinkedHashSet<>(List.of(UUID.randomUUID(), UUID.randomUUID())));

            assertEquals(1, statements.size());
        }
    }
}
