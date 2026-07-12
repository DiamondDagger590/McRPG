package us.eunoians.mcrpg.database.table.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import org.mockito.ArgumentCaptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Test
    @DisplayName("Given custom data, when saving then loading, then the structured data round-trips")
    public void customData_roundTripsThroughSaveAndLoad() throws SQLException {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_custom_data");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        QuestObjectiveInstance objective = stage.getQuestObjectives().get(0);
        objective.getCustomData().put("visited", new ArrayList<>(List.of("plains", "desert")));
        objective.markCustomDataDirty();

        // Save: capture the JSON written to the custom_data column (index 9 of the UPSERT).
        Connection saveConn = mock(Connection.class);
        PreparedStatement savePs = mock(PreparedStatement.class);
        when(saveConn.prepareStatement(anyString())).thenReturn(savePs);
        QuestObjectiveInstanceDAO.saveObjectiveInstance(saveConn, objective);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(savePs).setString(eq(9), jsonCaptor.capture());
        String customDataJson = jsonCaptor.getValue();
        assertNotNull(customDataJson);
        assertTrue(customDataJson.contains("plains"));

        // Load: feed the captured JSON back through a mocked ResultSet.
        Connection loadConn = mock(Connection.class);
        PreparedStatement loadPs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(loadConn.prepareStatement(anyString())).thenReturn(loadPs);
        when(loadPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("objective_uuid")).thenReturn(objective.getQuestObjectiveUUID().toString());
        when(rs.getString("definition_key")).thenReturn(objective.getQuestObjectiveKey().toString());
        when(rs.getString("state")).thenReturn(objective.getQuestObjectiveState().name());
        when(rs.getLong("required_progress")).thenReturn(objective.getRequiredProgression());
        when(rs.getLong("current_progress")).thenReturn(objective.getCurrentProgression());
        when(rs.getLong("start_time")).thenReturn(0L);
        when(rs.getLong("end_time")).thenReturn(0L);
        when(rs.wasNull()).thenReturn(true);
        when(rs.getString("custom_data")).thenReturn(customDataJson);

        List<QuestObjectiveInstance> loaded = QuestObjectiveInstanceDAO.loadObjectiveInstances(
                loadConn, stage.getQuestStageUUID(), stage);

        assertEquals(1, loaded.size());
        Object visited = loaded.get(0).getCustomData().get("visited");
        assertInstanceOf(List.class, visited);
        assertTrue(((List<?>) visited).contains("plains"));
        assertTrue(((List<?>) visited).contains("desert"));
    }

    @Test
    @DisplayName("Given empty custom data, when saving, then the custom_data column is null")
    public void saveObjectiveInstance_writesNullCustomData_whenEmpty() throws SQLException {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_empty_custom_data");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        QuestObjectiveInstance objective = stage.getQuestObjectives().get(0);

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        QuestObjectiveInstanceDAO.saveObjectiveInstance(conn, objective);

        verify(ps).setString(eq(9), isNull());
    }
}
