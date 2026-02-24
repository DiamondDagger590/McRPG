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

public class QuestObjectiveContributionDAOTest extends McRPGBaseTest {

    @DisplayName("Given an objective with contributions, when saving, then prepared statements are returned")
    @Test
    public void saveContributions_withContributions_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        UUID playerUUID = UUID.randomUUID();
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_contrib_test");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        QuestObjectiveInstance obj = stage.getQuestObjectives().get(0);
        obj.progress(5, playerUUID);

        List<PreparedStatement> result = QuestObjectiveContributionDAO.saveContributions(conn, obj);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @DisplayName("Given an objective without contributions, when saving, then empty list is returned")
    @Test
    public void saveContributions_noContributions_returnsEmptyList() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_contrib_empty");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        QuestObjectiveInstance obj = stage.getQuestObjectives().get(0);

        List<PreparedStatement> result = QuestObjectiveContributionDAO.saveContributions(conn, obj);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @DisplayName("Given an objective UUID, when deleting contributions, then prepared statements are returned")
    @Test
    public void deleteContributions_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        List<PreparedStatement> result = QuestObjectiveContributionDAO.deleteContributions(conn, UUID.randomUUID());
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
