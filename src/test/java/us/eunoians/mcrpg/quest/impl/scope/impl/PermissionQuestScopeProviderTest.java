package us.eunoians.mcrpg.quest.impl.scope.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionQuestScopeProviderTest extends McRPGBaseTest {

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

    @DisplayName("withPermissionNode returns this for chaining")
    @Test
    void withPermissionNode_returnsThisForChaining() {
        PermissionQuestScopeProvider provider = new PermissionQuestScopeProvider();
        PermissionQuestScopeProvider result = provider.withPermissionNode("mcrpg.quest.vip");
        assertEquals(provider, result);
    }

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
