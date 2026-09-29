package us.eunoians.mcrpg.quest.impl.scope.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SinglePlayerQuestScopeProviderTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns single_player_scope key")
        @Test
        void getKey_returnsSinglePlayerScopeKey() {
            SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();
            assertEquals(SinglePlayerQuestScopeProvider.KEY, provider.getKey());
            assertEquals("mcrpg:single_player_scope", provider.getKey().toString());
        }

        @DisplayName("getExpansionKey returns McRPG expansion key")
        @Test
        void getExpansionKey_returnsMcRPGExpansionKey() {
            SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();
            assertTrue(provider.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, provider.getExpansionKey().orElseThrow());
        }
    }

    @Nested
    @DisplayName("createNewScope")
    class CreateNewScope {

        @DisplayName("createNewScope returns scope with correct quest UUID")
        @Test
        void createNewScope_returnsScope_withCorrectQuestUUID() {
            SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();
            UUID questUUID = UUID.randomUUID();

            SinglePlayerQuestScope scope = provider.createNewScope(questUUID);

            assertNotNull(scope);
            assertEquals(questUUID, scope.getQuestUUID());
        }

        @DisplayName("createNewScope returns invalid scope (no player set)")
        @Test
        void createNewScope_returnsInvalidScope() {
            SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();

            SinglePlayerQuestScope scope = provider.createNewScope(UUID.randomUUID());

            assertTrue(scope.getPlayerInScope().isEmpty());
            assertTrue(scope.getCurrentPlayersInScope().isEmpty());
        }

        @DisplayName("Each createNewScope call returns a distinct scope")
        @Test
        void createNewScope_returnsDistinctScopes() {
            SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();
            UUID questUUID1 = UUID.randomUUID();
            UUID questUUID2 = UUID.randomUUID();

            SinglePlayerQuestScope scope1 = provider.createNewScope(questUUID1);
            SinglePlayerQuestScope scope2 = provider.createNewScope(questUUID2);

            assertEquals(questUUID1, scope1.getQuestUUID());
            assertEquals(questUUID2, scope2.getQuestUUID());
        }
    }

    @Nested
    @DisplayName("resolveActiveQuestUUIDs")
    class ResolveActiveQuestUUIDs {

        @Test
        @DisplayName("Given active quests in the database, when resolveActiveQuestUUIDs is called, then the quest UUIDs are returned")
        void resolveActiveQuestUUIDs_returnsQuestUUIDs_whenActiveQuestsExist() throws SQLException {
            SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();
            UUID playerUUID = UUID.randomUUID();
            UUID questUUID1 = UUID.randomUUID();
            UUID questUUID2 = UUID.randomUUID();

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getString("quest_uuid")).thenReturn(questUUID1.toString(), questUUID2.toString());

            List<UUID> result = provider.resolveActiveQuestUUIDs(playerUUID, connection);

            assertEquals(2, result.size());
            assertTrue(result.contains(questUUID1));
            assertTrue(result.contains(questUUID2));
            verify(ps).setString(1, playerUUID.toString());
        }

        @Test
        @DisplayName("Given no active quests, when resolveActiveQuestUUIDs is called, then an empty list is returned")
        void resolveActiveQuestUUIDs_returnsEmptyList_whenNoActiveQuests() throws SQLException {
            SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();
            UUID playerUUID = UUID.randomUUID();

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            List<UUID> result = provider.resolveActiveQuestUUIDs(playerUUID, connection);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("loadScope(Connection, UUID)")
    class LoadScope {

        @Test
        @DisplayName("Given a stored scope, when loadScope is called, then the scope has the correct player set")
        void loadScope_returnsScopeWithPlayer_whenStoredInDatabase() throws SQLException {
            SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();
            UUID questUUID = UUID.randomUUID();
            UUID playerUUID = UUID.randomUUID();

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("player_uuid")).thenReturn(playerUUID.toString());

            SinglePlayerQuestScope scope = provider.loadScope(connection, questUUID);

            assertNotNull(scope);
            assertEquals(questUUID, scope.getQuestUUID());
            assertTrue(scope.getPlayerInScope().isPresent());
            assertEquals(playerUUID, scope.getPlayerInScope().orElseThrow());
        }

        @Test
        @DisplayName("Given a stored scope, when loadScope is called, then the quest UUID parameter is passed to the DAO")
        void loadScope_passesQuestUUIDToDAO() throws SQLException {
            SinglePlayerQuestScopeProvider provider = new SinglePlayerQuestScopeProvider();
            UUID questUUID = UUID.randomUUID();
            UUID playerUUID = UUID.randomUUID();

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("player_uuid")).thenReturn(playerUUID.toString());

            provider.loadScope(connection, questUUID);

            verify(ps).setString(1, questUUID.toString());
        }
    }
}
