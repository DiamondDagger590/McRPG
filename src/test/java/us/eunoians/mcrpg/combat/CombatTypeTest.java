package us.eunoians.mcrpg.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CombatType")
class CombatTypeTest {

    @DisplayName("declares the expected values that round-trip through valueOf")
    @Test
    void values_roundTripThroughValueOf() {
        assertEquals(2, CombatType.values().length);
        for (CombatType type : CombatType.values()) {
            assertEquals(type, CombatType.valueOf(type.name()));
        }
    }
}
