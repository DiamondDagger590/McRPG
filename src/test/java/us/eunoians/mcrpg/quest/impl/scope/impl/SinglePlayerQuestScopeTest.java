package us.eunoians.mcrpg.quest.impl.scope.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.exception.quest.QuestScopeInvalidStateException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SinglePlayerQuestScope")
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

    @Nested
    @DisplayName("getScopeKey")
    class GetScopeKey {

        @Test
        @DisplayName("returns the single player scope key")
        void getScopeKey_returnsSinglePlayerScopeKey() {
            assertEquals("mcrpg:single_player_scope", scope.getScopeKey().toString());
        }
    }

    @Nested
    @DisplayName("getQuestUUID")
    class GetQuestUUID {

        @Test
        @DisplayName("returns the quest UUID from construction")
        void getQuestUUID_returnsConstructedUUID() {
            assertEquals(questUUID, scope.getQuestUUID());
        }
    }

    @Nested
    @DisplayName("setPlayerInScope")
    class SetPlayerInScope {

        @Test
        @DisplayName("succeeds when no player is set")
        void setPlayerInScope_succeedsOnce() {
            scope.setPlayerInScope(playerUUID);
            assertTrue(scope.getPlayerInScope().isPresent());
            assertEquals(playerUUID, scope.getPlayerInScope().get());
        }

        @Test
        @DisplayName("throws QuestScopeInvalidStateException when player is already set")
        void setPlayerInScope_throwsOnSecondCall() {
            scope.setPlayerInScope(playerUUID);
            assertThrows(QuestScopeInvalidStateException.class, () -> scope.setPlayerInScope(UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("isPlayerInScope")
    class IsPlayerInScope {

        @Test
        @DisplayName("returns true for the set player")
        void isPlayerInScope_returnsTrue_forSetPlayer() {
            scope.setPlayerInScope(playerUUID);
            assertTrue(scope.isPlayerInScope(playerUUID));
        }

        @Test
        @DisplayName("returns false for a different player")
        void isPlayerInScope_returnsFalse_forDifferentPlayer() {
            scope.setPlayerInScope(playerUUID);
            assertFalse(scope.isPlayerInScope(UUID.randomUUID()));
        }

        @Test
        @DisplayName("returns false when no player has been set")
        void isPlayerInScope_returnsFalse_whenNoPlayerSet() {
            assertFalse(scope.isPlayerInScope(playerUUID));
        }
    }

    @Nested
    @DisplayName("getCurrentPlayersInScope")
    class GetCurrentPlayersInScope {

        @Test
        @DisplayName("returns singleton set after player is set")
        void getCurrentPlayersInScope_returnsSingletonSet() {
            scope.setPlayerInScope(playerUUID);
            assertEquals(1, scope.getCurrentPlayersInScope().size());
            assertTrue(scope.getCurrentPlayersInScope().contains(playerUUID));
        }

        @Test
        @DisplayName("returns empty set before player is set")
        void getCurrentPlayersInScope_returnsEmptySet_beforePlayerSet() {
            assertTrue(scope.getCurrentPlayersInScope().isEmpty());
        }
    }

    @Nested
    @DisplayName("isScopeValid")
    class IsScopeValid {

        @Test
        @DisplayName("returns true after player is set")
        void isScopeValid_returnsTrue_afterPlayerSet() {
            scope.setPlayerInScope(playerUUID);
            assertTrue(scope.isScopeValid());
        }

        @Test
        @DisplayName("returns false before player is set")
        void isScopeValid_returnsFalse_beforePlayerSet() {
            assertFalse(scope.isScopeValid());
        }
    }

    @Nested
    @DisplayName("getPlayerInScope")
    class GetPlayerInScope {

        @Test
        @DisplayName("returns empty before player is set")
        void getPlayerInScope_returnsEmpty_beforePlayerSet() {
            assertTrue(scope.getPlayerInScope().isEmpty());
        }

        @Test
        @DisplayName("returns the player after it is set")
        void getPlayerInScope_returnsPlayer_afterPlayerSet() {
            scope.setPlayerInScope(playerUUID);
            assertEquals(playerUUID, scope.getPlayerInScope().orElseThrow());
        }
    }

    @Nested
    @DisplayName("saveScope")
    class SaveScope {

        @Test
        @DisplayName("throws QuestScopeInvalidStateException when scope is not valid")
        void saveScope_throwsWhenInvalid() {
            assertThrows(QuestScopeInvalidStateException.class,
                    () -> scope.saveScope(null));
        }
    }
}
