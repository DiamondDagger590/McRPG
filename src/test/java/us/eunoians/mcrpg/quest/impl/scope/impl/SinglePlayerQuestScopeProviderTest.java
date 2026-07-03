package us.eunoians.mcrpg.quest.impl.scope.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinglePlayerQuestScopeProviderTest extends McRPGBaseTest {

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
