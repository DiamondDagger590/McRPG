package us.eunoians.mcrpg.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ParticipantRemovalReason")
class ParticipantRemovalReasonTest {

    @DisplayName("declares the expected values that round-trip through valueOf")
    @Test
    void values_roundTripThroughValueOf() {
        assertEquals(7, ParticipantRemovalReason.values().length);
        for (ParticipantRemovalReason reason : ParticipantRemovalReason.values()) {
            assertEquals(reason, ParticipantRemovalReason.valueOf(reason.name()));
        }
    }
}
