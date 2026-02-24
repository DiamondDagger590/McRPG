package us.eunoians.mcrpg.database.table.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class QuestObjectiveInstanceDAOTest extends McRPGBaseTest {

    @DisplayName("Given an objective instance, when saving, then prepared statements are returned")
    @Test
    public void saveObjectiveInstance_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_obj_test");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        QuestObjectiveInstance obj = stage.getQuestObjectives().get(0);

        List<PreparedStatement> result = QuestObjectiveInstanceDAO.saveObjectiveInstance(conn, obj);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @DisplayName("Given an objective instance, when saving, then connection prepareStatement is called")
    @Test
    public void saveObjectiveInstance_usesConnection() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(mockPs);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_obj_conn");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        QuestObjectiveInstance obj = stage.getQuestObjectives().get(0);

        QuestObjectiveInstanceDAO.saveObjectiveInstance(conn, obj);
        verify(conn).prepareStatement(anyString());
    }

    @DisplayName("Given a stage with objectives, when saving all, then prepared statements are returned")
    @Test
    public void saveAllObjectiveInstances_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_all_obj");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);

        List<PreparedStatement> result = QuestObjectiveInstanceDAO.saveAllObjectiveInstances(conn, stage);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @DisplayName("Given a stage UUID, when deleting objectives, then prepared statements are returned")
    @Test
    public void deleteObjectiveInstances_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        List<PreparedStatement> result = QuestObjectiveInstanceDAO.deleteObjectiveInstances(conn, UUID.randomUUID());
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
