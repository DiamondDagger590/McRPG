package us.eunoians.mcrpg.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CombatSessionEndReason")
class CombatSessionEndReasonTest {

    @DisplayName("declares the expected values that round-trip through valueOf")
    @Test
    void values_roundTripThroughValueOf() {
        assertEquals(5, CombatSessionEndReason.values().length);
        for (CombatSessionEndReason reason : CombatSessionEndReason.values()) {
            assertEquals(reason, CombatSessionEndReason.valueOf(reason.name()));
        }
    }
}
