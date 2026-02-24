package us.eunoians.mcrpg.quest.impl.scope.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.exception.quest.QuestScopeInvalidStateException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SinglePlayerQuestScopeTest extends McRPGBaseTest {

    private SinglePlayerQuestScope scope;
    private UUID questUUID;
    private UUID playerUUID;

    @BeforeEach
    public void setup() {
        questUUID = UUID.randomUUID();
        playerUUID = UUID.randomUUID();
        scope = new SinglePlayerQuestScope(questUUID);
    }

    @DisplayName("Given a new scope, when setting player once, then it succeeds")
    @Test
    public void setPlayerInScope_succeedsOnce() {
        scope.setPlayerInScope(playerUUID);
        assertTrue(scope.getPlayerInScope().isPresent());
        assertEquals(playerUUID, scope.getPlayerInScope().get());
    }

    @DisplayName("Given a scope with player set, when setting again, then it throws QuestScopeInvalidStateException")
    @Test
    public void setPlayerInScope_throwsOnSecondCall() {
        scope.setPlayerInScope(playerUUID);
        assertThrows(QuestScopeInvalidStateException.class, () -> scope.setPlayerInScope(UUID.randomUUID()));
    }

    @DisplayName("Given a scope with player set, when checking isPlayerInScope, then it returns true for the set player")
    @Test
    public void isPlayerInScope_returnsTrue_forSetPlayer() {
        scope.setPlayerInScope(playerUUID);
        assertTrue(scope.isPlayerInScope(playerUUID));
    }

    @DisplayName("Given a scope with player set, when checking isPlayerInScope for a different player, then it returns false")
    @Test
    public void isPlayerInScope_returnsFalse_forDifferentPlayer() {
        scope.setPlayerInScope(playerUUID);
        assertFalse(scope.isPlayerInScope(UUID.randomUUID()));
    }

    @DisplayName("Given a scope with player set, when getting current players, then it returns a singleton set")
    @Test
    public void getCurrentPlayersInScope_returnsSingletonSet() {
        scope.setPlayerInScope(playerUUID);
        assertEquals(1, scope.getCurrentPlayersInScope().size());
        assertTrue(scope.getCurrentPlayersInScope().contains(playerUUID));
    }

    @DisplayName("Given a new scope without player, when getting current players, then it returns empty set")
    @Test
    public void getCurrentPlayersInScope_returnsEmptySet_beforePlayerSet() {
        assertTrue(scope.getCurrentPlayersInScope().isEmpty());
    }

    @DisplayName("Given a scope with player set, when checking isScopeValid, then it returns true")
    @Test
    public void isScopeValid_returnsTrue_afterPlayerSet() {
        scope.setPlayerInScope(playerUUID);
        assertTrue(scope.isScopeValid());
    }

    @DisplayName("Given a new scope, when checking isScopeValid, then it returns false")
    @Test
    public void isScopeValid_returnsFalse_beforePlayerSet() {
        assertFalse(scope.isScopeValid());
    }

    @DisplayName("Given a new scope, when getting player, then it returns empty")
    @Test
    public void getPlayerInScope_returnsEmpty_beforePlayerSet() {
        assertTrue(scope.getPlayerInScope().isEmpty());
    }
}
