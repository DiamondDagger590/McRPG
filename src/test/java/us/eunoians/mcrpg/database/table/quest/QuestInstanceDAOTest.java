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
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                System.currentTimeMillis(),
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
                1000L,
                2000L,
                null,
                new ManualQuestSource(),
                null
        );

        QuestInstanceDAO.saveFullQuestTree(mockConnection, instance);
        verify(mockConnection).prepareStatement(anyString());
    }
}
