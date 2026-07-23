package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.OptionalSavingAbilityAttribute;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("returns false when both tables exist")
        void attemptCreateTable_returnsFalse_whenBothTablesExist() {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_skill_data")).thenReturn(true);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_ability_attributes")).thenReturn(true);

            boolean result = SkillDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("returns true when skill data table is missing")
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
        @DisplayName("returns true when ability attribute table is missing")
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
        @DisplayName("returns true when both tables are missing")
        void attemptCreateTable_returnsTrue_whenBothTablesMissing() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_skill_data")).thenReturn(false);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_ability_attributes")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = SkillDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("getPlayerSkillLevelingData (3-arg: Connection, UUID, SkillDataSnapshot)")
    class GetPlayerSkillLevelingDataWithSnapshot {

        @Test
        @DisplayName("returns zero experience when no rows")
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
        @DisplayName("returns experience when row exists")
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
        @DisplayName("binds correct parameters")
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
    }

    @Nested
    @DisplayName("getPlayerSkillLevelingData (3-arg: Connection, UUID, NamespacedKey)")
    class GetPlayerSkillLevelingDataWithKey {

        @Test
        @DisplayName("creates snapshot with correct skill key")
        void getPlayerSkillLevelingData_createsSnapshotWithCorrectSkillKey() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            SkillDataSnapshot result = SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, SKILL_KEY);

            assertEquals(SKILL_KEY, result.getSkillKey());
            assertEquals(PLAYER_UUID, result.getUUID());
        }

        @Test
        @DisplayName("populates experience from query result")
        void getPlayerSkillLevelingData_populatesExperience() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("total_experience")).thenReturn(1200);

            SkillDataSnapshot result = SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, SKILL_KEY);

            assertEquals(1200, result.getTotalExperience());
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("sets version 1 for skill data table when no version exists")
        void updateTable_setsVersion1ForSkillData_whenNoVersionExists() {
            Connection mockConnection = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedVersionDAO = mockStatic(TableVersionHistoryDAO.class)) {
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_skill_data"))
                        .thenReturn(0);
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_ability_attributes"))
                        .thenReturn(1);
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, "mcrpg_skill_data", 1))
                        .thenReturn(true);

                SkillDAO.updateTable(mockConnection);

                mockedVersionDAO.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, "mcrpg_skill_data", 1));
            }
        }

        @Test
        @DisplayName("sets version 1 for ability attribute table when no version exists")
        void updateTable_setsVersion1ForAbilityAttributes_whenNoVersionExists() {
            Connection mockConnection = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedVersionDAO = mockStatic(TableVersionHistoryDAO.class)) {
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_skill_data"))
                        .thenReturn(1);
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_ability_attributes"))
                        .thenReturn(0);
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, "mcrpg_ability_attributes", 1))
                        .thenReturn(true);

                SkillDAO.updateTable(mockConnection);

                mockedVersionDAO.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, "mcrpg_ability_attributes", 1));
            }
        }

        @Test
        @DisplayName("does not update tables when both are at current version")
        void updateTable_doesNotUpdate_whenTablesAtCurrentVersion() {
            Connection mockConnection = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedVersionDAO = mockStatic(TableVersionHistoryDAO.class)) {
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_skill_data"))
                        .thenReturn(1);
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_ability_attributes"))
                        .thenReturn(1);

                SkillDAO.updateTable(mockConnection);

                mockedVersionDAO.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, "mcrpg_skill_data", 1),
                        org.mockito.Mockito.never());
                mockedVersionDAO.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, "mcrpg_ability_attributes", 1),
                        org.mockito.Mockito.never());
            }
        }
    }

    @Nested
    @DisplayName("savePlayerSkillData (single skill)")
    class SavePlayerSkillDataSingleSkill {

        @Test
        @DisplayName("produces REPLACE statement when experience is non-zero")
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
        @DisplayName("binds correct parameters for REPLACE statement")
        void savePlayerSkillData_bindsCorrectParams_forReplaceStatement() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData mockData = mock(SkillHolder.SkillHolderData.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(mockData));
            when(mockData.getTotalExperience()).thenReturn(750);

            SkillDAO.savePlayerSkillData(mockConnection, mockSkillHolder, SKILL_KEY);

            verify(mockReplaceStatement).setString(1, PLAYER_UUID.toString());
            verify(mockReplaceStatement).setString(2, SKILL_KEY.value());
            verify(mockReplaceStatement).setInt(3, 750);
        }

        @Test
        @DisplayName("produces DELETE statement when experience is zero")
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
        @DisplayName("produces empty list when no SkillHolderData")
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

    @Nested
    @DisplayName("savePlayerAbilityAttributes (with ability keys)")
    class SavePlayerAbilityAttributes {

        @Test
        @DisplayName("produces REPLACE statement for normal attribute")
        void savePlayerAbilityAttributes_producesReplace_forNormalAttribute() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "tier");

            AbilityAttribute<?> mockAttribute = mock(AbilityAttribute.class);
            when(mockAttribute.getDatabaseKeyName()).thenReturn("tier");
            when(mockAttribute.serializeContent()).thenReturn("3");

            AbilityData mockAbilityData = mock(AbilityData.class);
            when(mockAbilityData.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbilityData.getAllAttributeKeys()).thenReturn(Set.of(attributeKey));
            when(mockAbilityData.getAbilityAttribute(attributeKey)).thenReturn(Optional.of(mockAttribute));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.of(mockAbilityData));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(
                    mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertEquals(1, result.size());
            assertEquals(mockReplaceStatement, result.get(0));
            verify(mockReplaceStatement).setString(1, PLAYER_UUID.toString());
            verify(mockReplaceStatement).setString(2, abilityKey.value());
            verify(mockReplaceStatement).setString(3, "tier");
            verify(mockReplaceStatement).setString(4, "3");
        }

        @Test
        @DisplayName("produces DELETE statement for OptionalSavingAbilityAttribute that should not be saved")
        void savePlayerAbilityAttributes_producesDelete_forOptionalAttributeNotSaved() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "unlocked");

            OptionalSavingAbilityAttribute<?> mockOptionalAttribute = mock(OptionalSavingAbilityAttribute.class);
            when(mockOptionalAttribute.shouldContentBeSaved()).thenReturn(false);
            when(mockOptionalAttribute.getDatabaseKeyName()).thenReturn("unlocked");

            AbilityData mockAbilityData = mock(AbilityData.class);
            when(mockAbilityData.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbilityData.getAllAttributeKeys()).thenReturn(Set.of(attributeKey));
            when(mockAbilityData.getAbilityAttribute(attributeKey)).thenReturn(Optional.of(mockOptionalAttribute));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.of(mockAbilityData));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(
                    mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertEquals(1, result.size());
            assertEquals(mockDeleteStatement, result.get(0));
            verify(mockDeleteStatement).setString(1, PLAYER_UUID.toString());
            verify(mockDeleteStatement).setString(2, abilityKey.value());
            verify(mockDeleteStatement).setString(3, "unlocked");
        }

        @Test
        @DisplayName("produces REPLACE for OptionalSavingAbilityAttribute that should be saved")
        void savePlayerAbilityAttributes_producesReplace_forOptionalAttributeSaved() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "unlocked");

            OptionalSavingAbilityAttribute<?> mockOptionalAttribute = mock(OptionalSavingAbilityAttribute.class);
            when(mockOptionalAttribute.shouldContentBeSaved()).thenReturn(true);
            when(mockOptionalAttribute.getDatabaseKeyName()).thenReturn("unlocked");
            when(mockOptionalAttribute.serializeContent()).thenReturn("true");

            AbilityData mockAbilityData = mock(AbilityData.class);
            when(mockAbilityData.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbilityData.getAllAttributeKeys()).thenReturn(Set.of(attributeKey));
            when(mockAbilityData.getAbilityAttribute(attributeKey)).thenReturn(Optional.of(mockOptionalAttribute));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.of(mockAbilityData));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(
                    mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertEquals(1, result.size());
            assertEquals(mockReplaceStatement, result.get(0));
        }

        @Test
        @DisplayName("produces empty list when ability has no data on holder")
        void savePlayerAbilityAttributes_producesEmptyList_whenNoAbilityData() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.empty());

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(
                    mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("skips attribute when not registered in ability data")
        void savePlayerAbilityAttributes_skipsAttribute_whenNotRegistered() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "tier");

            AbilityData mockAbilityData = mock(AbilityData.class);
            when(mockAbilityData.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbilityData.getAllAttributeKeys()).thenReturn(Set.of(attributeKey));
            when(mockAbilityData.getAbilityAttribute(attributeKey)).thenReturn(Optional.empty());

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.of(mockAbilityData));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(
                    mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("saveAllSkillHolderInformation")
    class SaveAllSkillHolderInformation {

        @Test
        @DisplayName("combines results from skill data and ability attribute saves")
        void saveAllSkillHolderInformation_combinesResults() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            SkillRegistry skillRegistry = new SkillRegistry();
            RegistryAccess.registryAccess().register(skillRegistry);
            Swords swords = new Swords(mcRPG);
            skillRegistry.register(swords);

            AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
            RegistryAccess.registryAccess().register(abilityRegistry);

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            SkillHolder.SkillHolderData mockData = mock(SkillHolder.SkillHolderData.class);
            when(mockSkillHolder.getSkillHolderData(swords.getSkillKey())).thenReturn(Optional.of(mockData));
            when(mockData.getTotalExperience()).thenReturn(100);

            List<PreparedStatement> result = SkillDAO.saveAllSkillHolderInformation(mockConnection, mockSkillHolder);

            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list when holder has no data for any skill")
        void saveAllSkillHolderInformation_returnsEmptyList_whenNoData() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            SkillRegistry skillRegistry = new SkillRegistry();
            RegistryAccess.registryAccess().register(skillRegistry);

            AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
            RegistryAccess.registryAccess().register(abilityRegistry);

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);

            List<PreparedStatement> result = SkillDAO.saveAllSkillHolderInformation(mockConnection, mockSkillHolder);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAbilityAttributes (with NamespacedKey)")
    class GetAbilityAttributesWithKey {

        @Test
        @DisplayName("creates snapshot with correct skill key")
        void getAbilityAttributes_createsSnapshotWithCorrectSkillKey() throws SQLException {
            AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
            RegistryAccess.registryAccess().register(abilityAttributeRegistry);

            AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
            RegistryAccess.registryAccess().register(abilityRegistry);

            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            SkillDataSnapshot result = SkillDAO.getAbilityAttributes(mockConnection, PLAYER_UUID, SKILL_KEY);

            assertEquals(SKILL_KEY, result.getSkillKey());
            assertEquals(PLAYER_UUID, result.getUUID());
        }
    }

    @Nested
    @DisplayName("getAbilityAttributes (with SkillDataSnapshot)")
    class GetAbilityAttributesWithSnapshot {

        @Test
        @DisplayName("returns snapshot unchanged when skill has no abilities")
        void getAbilityAttributes_returnsUnchanged_whenSkillHasNoAbilities() throws SQLException {
            AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
            RegistryAccess.registryAccess().register(abilityAttributeRegistry);

            AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
            RegistryAccess.registryAccess().register(abilityRegistry);

            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            SkillDataSnapshot result = SkillDAO.getAbilityAttributes(mockConnection, PLAYER_UUID, snapshot);

            assertEquals(0, result.getTotalExperience());
            assertEquals(SKILL_KEY, result.getSkillKey());
        }
    }
}
