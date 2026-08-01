package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.util.TimeProvider;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DisplayName("CombatParticipant")
class CombatParticipantTest extends McRPGBaseTest {

    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        timeProvider = McRPG.getInstance().getTimeProvider();
    }

    @DisplayName("Constructor stores UUID")
    @Test
    void constructor_storesUUID() {
        UUID uuid = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");
        CombatParticipant participant = new CombatParticipant(uuid, ParticipantType.MOB, wrapper, 1000L);

        assertEquals(uuid, participant.getUUID());
    }

    @DisplayName("Constructor stores participantType")
    @Test
    void constructor_storesParticipantType() {
        UUID uuid = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");
        CombatParticipant participant = new CombatParticipant(uuid, ParticipantType.PLAYER, wrapper, 1000L);

        assertEquals(ParticipantType.PLAYER, participant.getParticipantType());
    }

    @DisplayName("Constructor stores entityWrapper")
    @Test
    void constructor_storesEntityWrapper() {
        UUID uuid = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");
        CombatParticipant participant = new CombatParticipant(uuid, ParticipantType.MOB, wrapper, 1000L);

        assertNotNull(participant.getEntityWrapper());
        assertEquals(wrapper, participant.getEntityWrapper());
    }

    @DisplayName("Constructor stores initial lastInteractionMillis")
    @Test
    void constructor_storesLastInteractionMillis() {
        UUID uuid = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");
        long initialMillis = 5000L;
        CombatParticipant participant = new CombatParticipant(uuid, ParticipantType.MOB, wrapper, initialMillis);

        assertEquals(initialMillis, participant.getLastInteractionMillis());
    }

    @DisplayName("setLastInteractionMillis updates the stored timestamp")
    @Test
    void setLastInteractionMillis_updatesTimestamp() {
        UUID uuid = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");
        CombatParticipant participant = new CombatParticipant(uuid, ParticipantType.MOB, wrapper, 1000L);

        participant.setLastInteractionMillis(9999L);

        assertEquals(9999L, participant.getLastInteractionMillis());
    }

    @DisplayName("isTimedOut returns false when within timeout window")
    @Test
    void isTimedOut_returnsFalse_whenWithinTimeoutWindow() {
        long nowMillis = timeProvider.now().toEpochMilli();
        UUID uuid = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");
        CombatParticipant participant = new CombatParticipant(uuid, ParticipantType.MOB, wrapper, nowMillis);

        long timeoutMillis = 8000L;

        assertFalse(participant.isTimedOut(timeoutMillis));
    }

    @DisplayName("isTimedOut returns true when past timeout window")
    @Test
    void isTimedOut_returnsTrue_whenPastTimeoutWindow() {
        long nowMillis = timeProvider.now().toEpochMilli();
        long timeoutMillis = 8000L;

        UUID uuid = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");
        CombatParticipant participant = new CombatParticipant(uuid, ParticipantType.MOB, wrapper, nowMillis);

        Instant futureInstant = Instant.ofEpochMilli(nowMillis + timeoutMillis + 1);
        when(timeProvider.now()).thenReturn(futureInstant);

        assertTrue(participant.isTimedOut(timeoutMillis));
    }

    @DisplayName("isTimedOut returns true at exactly the timeout boundary")
    @Test
    void isTimedOut_returnsTrue_whenAtExactTimeoutBoundary() {
        long nowMillis = timeProvider.now().toEpochMilli();
        long timeoutMillis = 8000L;

        UUID uuid = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");
        CombatParticipant participant = new CombatParticipant(uuid, ParticipantType.MOB, wrapper, nowMillis);

        Instant boundaryInstant = Instant.ofEpochMilli(nowMillis + timeoutMillis);
        when(timeProvider.now()).thenReturn(boundaryInstant);

        assertTrue(participant.isTimedOut(timeoutMillis));
    }
}
