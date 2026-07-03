package us.eunoians.mcrpg.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("CombatType")
class CombatTypeTest extends McRPGBaseTest {

    @DisplayName("PVE and PVP enum values exist")
    @Test
    void pveAndPvpValuesExist() {
        assertNotNull(CombatType.PVE);
        assertNotNull(CombatType.PVP);
    }

    @DisplayName("values() returns exactly two values")
    @Test
    void values_returnsExactlyTwo() {
        assertEquals(2, CombatType.values().length);
    }
}
