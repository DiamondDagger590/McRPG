package us.eunoians.mcrpg.quest.impl.scope.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionQuestScopeProviderTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns permission_scope key")
        @Test
        void getKey_returnsPermissionScopeKey() {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            assertEquals(PermissionQuestScope.PERMISSION_SCOPE_KEY, provider.getKey());
            assertEquals("mcrpg:permission_scope", provider.getKey().toString());
        }

        @DisplayName("getExpansionKey returns McRPG expansion key")
        @Test
        void getExpansionKey_returnsMcRPGExpansionKey() {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            assertTrue(provider.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, provider.getExpansionKey().orElseThrow());
        }
    }

    @Nested
    @DisplayName("withPermissionNode")
    class WithPermissionNode {

        @DisplayName("withPermissionNode returns this for chaining")
        @Test
        void withPermissionNode_returnsThisForChaining() {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            PermissionQuestScopeProvider result = provider.withPermissionNode("mcrpg.quest.vip");
            assertEquals(provider, result);
        }
    }

    @Nested
    @DisplayName("createNewScope")
    class CreateNewScope {

        @DisplayName("createNewScope returns scope with correct quest UUID and permission")
        @Test
        void createNewScope_returnsScope_withCorrectFields() {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            provider.withPermissionNode("mcrpg.quest.test");
            UUID questUUID = UUID.randomUUID();

            PermissionQuestScope scope = provider.createNewScope(questUUID);

            assertNotNull(scope);
            assertEquals(questUUID, scope.getQuestUUID());
            assertEquals("mcrpg.quest.test", scope.getPermissionNode().orElseThrow());
        }

        @DisplayName("createNewScope throws when permission not set")
        @Test
        void createNewScope_throwsIllegalStateException_whenPermissionNotSet() {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            assertThrows(IllegalStateException.class, () -> provider.createNewScope(UUID.randomUUID()));
        }

        @DisplayName("createNewScope returns valid scope")
        @Test
        void createNewScope_returnsValidScope() {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            provider.withPermissionNode("mcrpg.quest.test");

            PermissionQuestScope scope = provider.createNewScope(UUID.randomUUID());

            assertTrue(scope.isScopeValid());
        }

        @DisplayName("Each createNewScope call returns a distinct scope")
        @Test
        void createNewScope_returnsDistinctScopes() {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            provider.withPermissionNode("mcrpg.quest.test");
            UUID questUUID1 = UUID.randomUUID();
            UUID questUUID2 = UUID.randomUUID();

            PermissionQuestScope scope1 = provider.createNewScope(questUUID1);
            PermissionQuestScope scope2 = provider.createNewScope(questUUID2);

            assertEquals(questUUID1, scope1.getQuestUUID());
            assertEquals(questUUID2, scope2.getQuestUUID());
        }
    }

    @Nested
    @DisplayName("resolveActiveQuestUUIDs")
    class ResolveActiveQuestUUIDs {

        @Test
        @DisplayName("Given active permission quests and an online player with matching permission, when resolveActiveQuestUUIDs is called, then matching quest UUIDs are returned")
        void resolveActiveQuestUUIDs_returnsMatching_whenOnlinePlayerHasPermission() throws SQLException {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            PlayerMock playerMock = server.addPlayer();
            UUID playerUUID = playerMock.getUniqueId();
            UUID questUUID = UUID.randomUUID();
            String permNode = "mcrpg.quest.vip";

            playerMock.addAttachment(mcRPG, permNode, true);

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("quest_uuid")).thenReturn(questUUID.toString());
            when(rs.getString("permission_node")).thenReturn(permNode);

            List<UUID> result = provider.resolveActiveQuestUUIDs(playerUUID, connection);

            assertEquals(1, result.size());
            assertEquals(questUUID, result.get(0));
        }

        @Test
        @DisplayName("Given active permission quests and an online player without the permission, when resolveActiveQuestUUIDs is called, then empty list is returned")
        void resolveActiveQuestUUIDs_returnsEmpty_whenOnlinePlayerLacksPermission() throws SQLException {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            PlayerMock playerMock = server.addPlayer();
            UUID playerUUID = playerMock.getUniqueId();
            UUID questUUID = UUID.randomUUID();

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("quest_uuid")).thenReturn(questUUID.toString());
            when(rs.getString("permission_node")).thenReturn("mcrpg.quest.vip");

            List<UUID> result = provider.resolveActiveQuestUUIDs(playerUUID, connection);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given no active permission quests in the database, when resolveActiveQuestUUIDs is called, then empty list is returned")
        void resolveActiveQuestUUIDs_returnsEmpty_whenNoActiveQuests() throws SQLException {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
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

        @Test
        @DisplayName("Given multiple active quests, when player has permission for only one, then only that quest UUID is returned")
        void resolveActiveQuestUUIDs_filtersCorrectly_whenPlayerHasPartialPermissions() throws SQLException {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            PlayerMock playerMock = server.addPlayer();
            UUID playerUUID = playerMock.getUniqueId();
            UUID questWithPerm = UUID.randomUUID();
            UUID questWithoutPerm = UUID.randomUUID();

            playerMock.addAttachment(mcRPG, "mcrpg.quest.vip", true);

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getString("quest_uuid")).thenReturn(questWithPerm.toString(), questWithoutPerm.toString());
            when(rs.getString("permission_node")).thenReturn("mcrpg.quest.vip", "mcrpg.quest.admin");

            List<UUID> result = provider.resolveActiveQuestUUIDs(playerUUID, connection);

            assertEquals(1, result.size());
            assertEquals(questWithPerm, result.get(0));
        }
    }

    @Nested
    @DisplayName("loadScope(Connection, UUID)")
    class LoadScope {

        @Test
        @DisplayName("Given a stored permission scope, when loadScope is called, then the scope has the correct permission node")
        void loadScope_returnsScopeWithPermission_whenStoredInDatabase() throws SQLException {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            UUID questUUID = UUID.randomUUID();
            String permNode = "mcrpg.quest.vip";

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("permission_node")).thenReturn(permNode);

            PermissionQuestScope scope = provider.loadScope(connection, questUUID);

            assertNotNull(scope);
            assertEquals(questUUID, scope.getQuestUUID());
            assertTrue(scope.getPermissionNode().isPresent());
            assertEquals(permNode, scope.getPermissionNode().orElseThrow());
        }

        @Test
        @DisplayName("Given a stored permission scope, when loadScope is called, then the quest UUID is passed to the DAO")
        void loadScope_passesQuestUUIDToDAO() throws SQLException {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            UUID questUUID = UUID.randomUUID();

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("permission_node")).thenReturn("mcrpg.quest.test");

            provider.loadScope(connection, questUUID);

            verify(ps).setString(1, questUUID.toString());
        }

        @Test
        @DisplayName("Given a loaded scope, when isScopeValid is called, then it returns true")
        void loadScope_returnsValidScope() throws SQLException {
            PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
            UUID questUUID = UUID.randomUUID();

            Connection connection = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(connection.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("permission_node")).thenReturn("mcrpg.quest.test");

            PermissionQuestScope scope = provider.loadScope(connection, questUUID);

            assertTrue(scope.isScopeValid());
        }
    }
}
