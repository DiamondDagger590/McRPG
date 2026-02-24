package us.eunoians.mcrpg.quest.impl.scope.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LandQuestScopeTest extends McRPGBaseTest {

    private static boolean isLandsApiAvailable() {
        try {
            Class.forName("me.angeschossen.lands.api.land.Land");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private LandQuestScope scope;
    private UUID questUUID;

    @BeforeEach
    public void setup() {
        questUUID = UUID.randomUUID();
        scope = new LandQuestScope(questUUID, null);
    }

    @DisplayName("Given a new scope, when setting land name once, then it succeeds")
    @Test
    public void setLandName_succeedsOnce() {
        scope.setLandName("my_land");
        assertTrue(scope.getLandName().isPresent());
    }

    @DisplayName("Given a scope with land name set, when setting again, then it throws IllegalStateException")
    @Test
    public void setLandName_throwsOnSecondCall() {
        scope.setLandName("my_land");
        assertThrows(IllegalStateException.class, () -> scope.setLandName("other_land"));
    }

    @DisplayName("Given a scope without land name, when checking isScopeValid, then it returns false")
    @Test
    public void isScopeValid_returnsFalse_whenLandNameNull() {
        assertFalse(scope.isScopeValid());
    }

    @DisplayName("Given a scope with land name but no Lands plugin, when checking isScopeValid, then it returns false")
    @Test
    @EnabledIf("isLandsApiAvailable")
    public void isScopeValid_returnsFalse_whenLandsNotAvailable() {
        scope.setLandName("my_land");
        assertFalse(scope.isScopeValid());
    }

    @DisplayName("Given a scope without Lands plugin, when getting players, then it returns empty set")
    @Test
    @EnabledIf("isLandsApiAvailable")
    public void getCurrentPlayersInScope_returnsEmpty_whenLandsNotAvailable() {
        scope.setLandName("my_land");
        assertTrue(scope.getCurrentPlayersInScope().isEmpty());
    }

    @DisplayName("Given a scope without Lands plugin, when checking player membership, then it returns false")
    @Test
    @EnabledIf("isLandsApiAvailable")
    public void isPlayerInScope_returnsFalse_whenLandsNotAvailable() {
        scope.setLandName("my_land");
        assertFalse(scope.isPlayerInScope(UUID.randomUUID()));
    }
}
