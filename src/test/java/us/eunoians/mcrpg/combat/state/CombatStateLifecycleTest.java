package us.eunoians.mcrpg.combat.state;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CombatStateLifecycle")
class CombatStateLifecycleTest {

    @DisplayName("declared values round-trip through valueOf")
    @Test
    void valueOf_roundTripsDeclaredValues() {
        assertEquals(CombatStateLifecycle.SESSION, CombatStateLifecycle.valueOf("SESSION"));
        assertEquals(CombatStateLifecycle.PERSISTENT, CombatStateLifecycle.valueOf("PERSISTENT"));
    }

    @DisplayName("values() contains exactly SESSION and PERSISTENT")
    @Test
    void values_containsExactlyExpectedConstants() {
        assertEquals(2, CombatStateLifecycle.values().length);
    }
}
