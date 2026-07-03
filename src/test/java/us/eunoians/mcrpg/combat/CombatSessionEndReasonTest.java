package us.eunoians.mcrpg.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("CombatSessionEndReason")
class CombatSessionEndReasonTest extends McRPGBaseTest {

    @DisplayName("TIMEOUT enum value exists")
    @Test
    void timeoutExists() {
        assertNotNull(CombatSessionEndReason.TIMEOUT);
    }

    @DisplayName("DEATH enum value exists")
    @Test
    void deathExists() {
        assertNotNull(CombatSessionEndReason.DEATH);
    }

    @DisplayName("LOGOUT enum value exists")
    @Test
    void logoutExists() {
        assertNotNull(CombatSessionEndReason.LOGOUT);
    }

    @DisplayName("ALL_PARTICIPANTS_GONE enum value exists")
    @Test
    void allParticipantsGoneExists() {
        assertNotNull(CombatSessionEndReason.ALL_PARTICIPANTS_GONE);
    }

    @DisplayName("PLUGIN enum value exists")
    @Test
    void pluginExists() {
        assertNotNull(CombatSessionEndReason.PLUGIN);
    }
}
