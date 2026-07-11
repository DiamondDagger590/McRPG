package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link SkillDAO}.
 * <p>
 * The JDBC layer is fully mocked so the tests run without a real database.
 */
class SkillDAOTest extends McRPGBaseTest {

    private static final NamespacedKey SKILL_KEY = new NamespacedKey("mcrpg", "swords");
    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Test
    @DisplayName("attemptCreateTable returns false when both tables exist")
    void attemptCreateTable_returnsFalse_whenBothTablesExist() {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(mockConnection, "mcrpg_skill_data")).thenReturn(true);
        when(mockDatabase.tableExists(mockConnection, "mcrpg_ability_attributes")).thenReturn(true);

        boolean result = SkillDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertFalse(result);
    }

    @Test
    @DisplayName("attemptCreateTable returns true when skill data table is missing")
    void attemptCreateTable_returnsTrue_whenSkillDataTableMissing() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockDatabase.tableExists(mockConnection, "mcrpg_skill_data")).thenReturn(false);
        when(mockDatabase.tableExists(mockConnection, "mcrpg_ability_attributes")).thenReturn(true);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        boolean result = SkillDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertTrue(result);
    }

    @Test
    @DisplayName("attemptCreateTable returns true when ability attribute table is missing")
    void attemptCreateTable_returnsTrue_whenAbilityAttributeTableMissing() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockDatabase.tableExists(mockConnection, "mcrpg_skill_data")).thenReturn(true);
        when(mockDatabase.tableExists(mockConnection, "mcrpg_ability_attributes")).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        boolean result = SkillDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertTrue(result);
    }

    @Test
    @DisplayName("getPlayerSkillLevelingData returns zero experience when no rows")
    void getPlayerSkillLevelingData_returnsZeroExperience_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
        SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, snapshot);

        assertEquals(0, snapshot.getTotalExperience());
    }

    @Test
    @DisplayName("getPlayerSkillLevelingData returns experience when row exists")
    void getPlayerSkillLevelingData_returnsExperience_whenRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("total_experience")).thenReturn(500);

        SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
        SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, snapshot);

        assertEquals(500, snapshot.getTotalExperience());
    }

    @Test
    @DisplayName("getPlayerSkillLevelingData binds correct parameters")
    void getPlayerSkillLevelingData_bindsCorrectParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
        SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, snapshot);

        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setString(2, "swords");
    }

    @Test
    @DisplayName("savePlayerSkillData produces REPLACE statement when experience is non-zero")
    void savePlayerSkillData_producesReplaceStatement_whenExperienceNonZero() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
        PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
        when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

        SkillHolder mockSkillHolder = mock(SkillHolder.class);
        SkillHolder.SkillHolderData mockData = mock(SkillHolder.SkillHolderData.class);
        when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
        when(mockSkillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(mockData));
        when(mockData.getTotalExperience()).thenReturn(100);

        List<PreparedStatement> result = SkillDAO.savePlayerSkillData(mockConnection, mockSkillHolder, SKILL_KEY);

        assertEquals(1, result.size());
        assertEquals(mockReplaceStatement, result.get(0));
    }

    @Test
    @DisplayName("savePlayerSkillData produces DELETE statement when experience is zero")
    void savePlayerSkillData_producesDeleteStatement_whenExperienceZero() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
        PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
        when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

        SkillHolder mockSkillHolder = mock(SkillHolder.class);
        SkillHolder.SkillHolderData mockData = mock(SkillHolder.SkillHolderData.class);
        when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
        when(mockSkillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(mockData));
        when(mockData.getTotalExperience()).thenReturn(0);

        List<PreparedStatement> result = SkillDAO.savePlayerSkillData(mockConnection, mockSkillHolder, SKILL_KEY);

        assertEquals(1, result.size());
        assertEquals(mockDeleteStatement, result.get(0));
    }

    @Test
    @DisplayName("savePlayerSkillData produces empty list when no SkillHolderData")
    void savePlayerSkillData_producesEmptyList_whenNoSkillHolderData() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
        PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
        when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

        SkillHolder mockSkillHolder = mock(SkillHolder.class);
        when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
        when(mockSkillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.empty());

        List<PreparedStatement> result = SkillDAO.savePlayerSkillData(mockConnection, mockSkillHolder, SKILL_KEY);

        assertTrue(result.isEmpty());
    }
}
