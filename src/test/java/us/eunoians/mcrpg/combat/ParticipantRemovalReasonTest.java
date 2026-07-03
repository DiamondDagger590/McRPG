package us.eunoians.mcrpg.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("ParticipantRemovalReason")
class ParticipantRemovalReasonTest extends McRPGBaseTest {

    @DisplayName("DEATH enum value exists")
    @Test
    void deathExists() {
        assertNotNull(ParticipantRemovalReason.DEATH);
    }

    @DisplayName("LOGOUT enum value exists")
    @Test
    void logoutExists() {
        assertNotNull(ParticipantRemovalReason.LOGOUT);
    }

    @DisplayName("DESPAWN enum value exists")
    @Test
    void despawnExists() {
        assertNotNull(ParticipantRemovalReason.DESPAWN);
    }

    @DisplayName("TIMEOUT enum value exists")
    @Test
    void timeoutExists() {
        assertNotNull(ParticipantRemovalReason.TIMEOUT);
    }

    @DisplayName("EVICTION enum value exists")
    @Test
    void evictionExists() {
        assertNotNull(ParticipantRemovalReason.EVICTION);
    }

    @DisplayName("SESSION_END enum value exists")
    @Test
    void sessionEndExists() {
        assertNotNull(ParticipantRemovalReason.SESSION_END);
    }
}
