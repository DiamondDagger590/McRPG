package us.eunoians.mcrpg.database.table.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class QuestStageInstanceDAOTest extends McRPGBaseTest {

    @DisplayName("Given a stage instance, when saving, then prepared statements are returned")
    @Test
    public void saveStageInstance_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_test");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);

        List<PreparedStatement> result = QuestStageInstanceDAO.saveStageInstance(conn, stage);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @DisplayName("Given a stage instance, when saving, then connection prepareStatement is called")
    @Test
    public void saveStageInstance_usesConnection() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(mockPs);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_conn");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);

        QuestStageInstanceDAO.saveStageInstance(conn, stage);
        verify(conn).prepareStatement(anyString());
    }

    @DisplayName("Given a quest with stages, when saving all, then prepared statements are returned")
    @Test
    public void saveAllStageInstances_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_all_stages");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

        List<PreparedStatement> result = QuestStageInstanceDAO.saveAllStageInstances(conn, quest);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @DisplayName("Given a quest UUID, when deleting stages, then prepared statements are returned")
    @Test
    public void deleteStageInstances_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        List<PreparedStatement> result = QuestStageInstanceDAO.deleteStageInstances(conn, UUID.randomUUID());
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
