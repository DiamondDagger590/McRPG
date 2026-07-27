package us.eunoians.mcrpg.combat.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.combat.CombatType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatLogMode")
class CombatLogModeTest {

    @Test
    @DisplayName("valueOf round-trips the expected enum constants")
    void valueOf_roundTripsExpectedConstants() {
        assertEquals(CombatLogMode.DISABLED, CombatLogMode.valueOf("DISABLED"));
        assertEquals(CombatLogMode.PLAYERS, CombatLogMode.valueOf("PLAYERS"));
        assertEquals(CombatLogMode.MOBS_AND_PLAYERS, CombatLogMode.valueOf("MOBS_AND_PLAYERS"));
    }

    @Test
    @DisplayName("DISABLED.shouldPunish returns false for PVE")
    void disabled_shouldPunish_returnsFalse_forPve() {
        assertFalse(CombatLogMode.DISABLED.shouldPunish(CombatType.PVE));
    }

    @Test
    @DisplayName("DISABLED.shouldPunish returns false for PVP")
    void disabled_shouldPunish_returnsFalse_forPvp() {
        assertFalse(CombatLogMode.DISABLED.shouldPunish(CombatType.PVP));
    }

    @Test
    @DisplayName("PLAYERS.shouldPunish returns false for PVE")
    void players_shouldPunish_returnsFalse_forPve() {
        assertFalse(CombatLogMode.PLAYERS.shouldPunish(CombatType.PVE));
    }

    @Test
    @DisplayName("PLAYERS.shouldPunish returns true for PVP")
    void players_shouldPunish_returnsTrue_forPvp() {
        assertTrue(CombatLogMode.PLAYERS.shouldPunish(CombatType.PVP));
    }

    @Test
    @DisplayName("MOBS_AND_PLAYERS.shouldPunish returns true for PVE")
    void mobsAndPlayers_shouldPunish_returnsTrue_forPve() {
        assertTrue(CombatLogMode.MOBS_AND_PLAYERS.shouldPunish(CombatType.PVE));
    }

    @Test
    @DisplayName("MOBS_AND_PLAYERS.shouldPunish returns true for PVP")
    void mobsAndPlayers_shouldPunish_returnsTrue_forPvp() {
        assertTrue(CombatLogMode.MOBS_AND_PLAYERS.shouldPunish(CombatType.PVP));
    }

    @Test
    @DisplayName("DISABLED.isEnabled returns false")
    void disabled_isEnabled_returnsFalse() {
        assertFalse(CombatLogMode.DISABLED.isEnabled());
    }

    @Test
    @DisplayName("PLAYERS.isEnabled returns true")
    void players_isEnabled_returnsTrue() {
        assertTrue(CombatLogMode.PLAYERS.isEnabled());
    }

    @Test
    @DisplayName("MOBS_AND_PLAYERS.isEnabled returns true")
    void mobsAndPlayers_isEnabled_returnsTrue() {
        assertTrue(CombatLogMode.MOBS_AND_PLAYERS.isEnabled());
    }
}
