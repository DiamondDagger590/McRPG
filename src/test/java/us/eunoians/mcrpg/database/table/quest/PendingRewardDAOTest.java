package us.eunoians.mcrpg.database.table.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.PendingReward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
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
}
